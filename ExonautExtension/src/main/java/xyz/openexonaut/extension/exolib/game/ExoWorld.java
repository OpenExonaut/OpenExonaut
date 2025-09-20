package xyz.openexonaut.extension.exolib.game;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.map.*;
import xyz.openexonaut.extension.exolib.resources.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class ExoWorld extends ExoTickable {
    public final ExoMap map;
    public final ExoItem[] items;

    private final Room room;

    private World world = new World(new Vector2(0f, -100f), true);

    // this "efficiently" handles concurrency automagically; the copy-on-write arraylist is probably
    // slower given the volatility
    private final Set<ExoBullet> activeBullets = ConcurrentHashMap.newKeySet();

    static {
        Box2D.init();
    }

    public ExoWorld(ExoMap map, Room room) {
        this.map = map;
        this.room = room;

        Body walls = world.createBody(ExoDefs.wallDef);
        for (FixtureDef wall : map.wallFixtureDefs) {
            walls.createFixture(wall).setUserData(new ExoUserData(0, 0));
        }

        if (!map.finalized()) {
            for (ExoItemSpawner spawner : map.teamItemSpawns) {
                finalizeItemSpawner(spawner);
            }
            for (ExoItemSpawner spawner : map.ffaItemSpawns) {
                finalizeItemSpawner(spawner);
            }
            map.finishedFinalization();
        }

        if (room.getVariable("mode").getStringValue().equals("team")) {
            items = new ExoItem[map.teamItemSpawns.length];
            for (int i = 0; i < items.length; i++) {
                items[i] = new ExoItem(map.teamItemSpawns[i]);
            }
        } else {
            items = new ExoItem[map.ffaItemSpawns.length];
            for (int i = 0; i < items.length; i++) {
                items[i] = new ExoItem(map.ffaItemSpawns[i]);
            }
        }
    }

    public void destroy() {
        world.dispose();
        world = null;
    }

    private void finalizeItemSpawner(ExoItemSpawner spawner) {
        float center = pickupSpawnerRaycast(spawner.x, spawner.y);
        float left = pickupSpawnerRaycast(spawner.x - 5f, spawner.y);
        float right = pickupSpawnerRaycast(spawner.x + 5f, spawner.y);

        spawner.finalize(Math.min(Math.max(center, Math.max(left, right)), 12f));
    }

    public void spawnPlayer(int id) {
        ExoPlayer player = (ExoPlayer) room.getPlayersList().get(id - 1).getProperty("ExoPlayer");
        ExoDefs.playerDef.position.set(player.getX(), player.getY());
        ExoDefs.setFilters(ExoFilterUtils.getPlayerCategory(id), ExoFilterUtils.getPlayerMask(id));

        Body newPlayerBody = world.createBody(ExoDefs.playerDef);
        newPlayerBody.createFixture(ExoDefs.standingHeadDef).setUserData(new ExoUserData(id, 1));
        newPlayerBody.createFixture(ExoDefs.standingBodyDef).setUserData(new ExoUserData(id, 2));
        newPlayerBody.createFixture(ExoDefs.standingFeetDef).setUserData(new ExoUserData(id, 3));
        player.setBody(newPlayerBody);
    }

    public boolean spawnBullet(ExoBullet bullet) {
        return activeBullets.add(bullet);
    }

    public void handleSnipe(ExoBullet bullet) {
        float x = bullet.getX();
        float y = bullet.getY();

        Vector2 delta = new Vector2(bullet.velocityXComponent - x, bullet.velocityYComponent - y);
        delta.setLength(1000f);

        ExoUserData raycastData =
                bulletRaycast(x, y, x + delta.x, y + delta.y, bullet.player.user.getPlayerId());
        if (raycastData != null) {
            if (raycastData.id != 0) {
                ISFSArray eventArray = new SFSArray();
                ((ExoPlayer) room.getPlayersList().get(raycastData.id - 1).getProperty("ExoPlayer"))
                        .hit(bullet, raycastData.part, eventArray);
                ExoSendUtils.sendEventArrayToAll(room, eventArray);
            }
        }
    }

    public void draw(Graphics g) {
        map.draw(g);

        g.translate((int) map.translate.x, (int) map.translate.y);

        for (ExoItem item : items) {
            item.draw(g, map.scale);
        }

        for (User user : room.getPlayersList()) {
            if (user != null) {
                ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");
                player.draw(g, map);
            }
        }

        g.setColor(Color.ORANGE);
        for (ExoBullet bullet : activeBullets) {
            bullet.draw(g, map);
        }

        g.translate(-(int) map.translate.x, -(int) map.translate.y);
    }

    @Override
    public float tick(ISFSArray eventQueue) {
        float deltaTime = super.tick(eventQueue);

        for (User user : room.getPlayersList()) {
            if (user != null) {
                ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");
                player.tick(eventQueue);
                player.getBody().setTransform(new Vector2(player.getX(), player.getY()), 0f);
            }
        }

        for (ExoItem item : items) {
            item.tick(eventQueue);
        }

        simulateBullets(eventQueue);

        return deltaTime;
    }

    public void simulateBullets(ISFSArray eventQueue) {
        List<ExoBullet> expiringBullets = new ArrayList<>(activeBullets.size());
        Map<ExoBullet, ExoUserData> playerHits = new HashMap<>();

        for (ExoBullet bullet : activeBullets) {
            boolean last = false;

            float raycastDist = bullet.velocity * bullet.tick(eventQueue);
            float remainingDist = bullet.range - bullet.getDist();

            if (raycastDist >= remainingDist) {
                last = true;
                raycastDist = remainingDist;
            }

            float raycastX = bullet.velocityXComponent * raycastDist;
            float raycastY = bullet.velocityYComponent * raycastDist;

            float x = bullet.getX();
            float y = bullet.getY();

            ExoUserData raycastData =
                    bulletRaycast(
                            x, y, x + raycastX, y + raycastY, bullet.player.user.getPlayerId());

            if (raycastData != null) {
                expiringBullets.add(bullet);
                if (raycastData.id != 0) {
                    playerHits.put(bullet, raycastData);
                }
            } else {
                if (last) {
                    expiringBullets.add(bullet);
                } else {
                    bullet.addDist(raycastDist);
                    bullet.addX(raycastX);
                    bullet.addY(raycastY);
                }
            }
        }

        activeBullets.removeAll(expiringBullets);
        for (ExoBullet bullet : playerHits.keySet()) {
            ExoUserData hitData = playerHits.get(bullet);
            ((ExoPlayer) room.getPlayersList().get(hitData.id - 1).getProperty("ExoPlayer"))
                    .hit(bullet, hitData.part, eventQueue);
        }
    }

    private ExoUserData bulletRaycast(
            float startX, float startY, float endX, float endY, int shooterID) {
        ExoRaycastHandler raycastHandler =
                new ExoRaycastHandler(exoUserData -> exoUserData.id != shooterID);

        world.rayCast(raycastHandler, startX, startY, endX, endY);

        raycastHandler.backcast = true;
        world.rayCast(raycastHandler, endX, endY, startX, startY);

        return raycastHandler.result;
    }

    private float pickupSpawnerRaycast(float startX, float startY) {
        ExoRaycastHandler raycastHandler =
                new ExoRaycastHandler(exoUserData -> exoUserData.id == 0);

        world.rayCast(raycastHandler, startX, startY, startX, startY - 12f);

        raycastHandler.backcast = true;
        world.rayCast(raycastHandler, startX, startY - 12f, startX, startY);

        return raycastHandler.raycastFraction * 12f;
    }

    private static class ExoRaycastHandler implements RayCastCallback {
        public final Predicate<ExoUserData> test;

        public ExoUserData result = null;
        public float raycastFraction = 1f;
        public boolean backcast = false;

        public ExoRaycastHandler(Predicate<ExoUserData> test) {
            this.test = test;
        }

        @Override
        public float reportRayFixture(
                Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if (backcast) {
                float currentFraction = 1f - fraction;
                if (currentFraction < raycastFraction) {
                    ExoUserData exoUserData = (ExoUserData) fixture.getUserData();
                    if (test.test(exoUserData)) {
                        result = exoUserData;
                        raycastFraction = currentFraction;
                    }
                }
            } else {
                ExoUserData exoUserData = (ExoUserData) fixture.getUserData();
                if (test.test(exoUserData)) {
                    result = exoUserData;
                    raycastFraction = fraction;
                    return fraction;
                }
            }
            return 1f;
        }
    }

    private static class ExoUserData {
        public final int id;
        public final int part;

        private ExoUserData(int id, int part) {
            this.id = id;
            this.part = part;
        }
    }
}
