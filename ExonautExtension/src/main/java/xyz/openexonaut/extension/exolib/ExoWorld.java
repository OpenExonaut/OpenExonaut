package xyz.openexonaut.extension.exolib;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;

import com.smartfoxserver.v2.entities.*;

public class ExoWorld {
    public final ExoMap map;
    private final Room room;

    private final World world = new World(new Vector2(0f, -100f), true);

    private final FixtureDef playerHeadDef = new FixtureDef();
    private final FixtureDef playerBodyDef = new FixtureDef();
    private final FixtureDef playerFeetDef = new FixtureDef();

    private final BodyDef wallDef = new BodyDef();
    private final BodyDef playerDef = new BodyDef();

    private final Body walls;

    public final ExoItem[] items;

    private long lastNano = System.nanoTime();

    // this "efficiently" handles concurrency automagically; the copy-on-write arraylist is probably
    // slower given the volatility
    private final Set<ExoBullet> activeBullets = ConcurrentHashMap.newKeySet();

    public ExoWorld(ExoMap map, Room room) {
        this.map = map;
        this.room = room;

        wallDef.awake = false;
        playerDef.fixedRotation = true;

        walls = world.createBody(wallDef);
        for (FixtureDef wall : map.wallFixtures) {
            walls.createFixture(wall).setUserData(new ExoUserData(0, 0));
        }

        CircleShape head = new CircleShape();
        head.setPosition(new Vector2(0f, 11.5f));
        head.setRadius(1.5f);

        PolygonShape body = new PolygonShape();
        body.setAsBox(1.5f, 5f, new Vector2(0f, 6.5f), 0f);

        CircleShape feet = new CircleShape();
        feet.setPosition(new Vector2(0f, 1.5f));
        feet.setRadius(1.5f);

        playerHeadDef.shape = head;
        playerBodyDef.shape = body;
        playerFeetDef.shape = feet;

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

    private void finalizeItemSpawner(ExoItemSpawner spawner) {
        float center = pickupSpawnerRaycast(spawner.x, spawner.y);
        float left = pickupSpawnerRaycast(spawner.x - 5f, spawner.y);
        float right = pickupSpawnerRaycast(spawner.x + 5f, spawner.y);

        spawner.finalize(Math.min(Math.max(center, Math.max(left, right)), 12f));
    }

    public void spawnPlayer(int id) {
        ExoPlayer player = (ExoPlayer) room.getPlayersList().get(id - 1).getProperty("ExoPlayer");
        playerDef.position.set(player.getX(), player.getY());
        player.body = world.createBody(playerDef);
        player.body.createFixture(playerHeadDef).setUserData(new ExoUserData(id, 1));
        player.body.createFixture(playerBodyDef).setUserData(new ExoUserData(id, 2));
        player.body.createFixture(playerFeetDef).setUserData(new ExoUserData(id, 3));
    }

    public boolean spawnBullet(ExoBullet bullet) {
        return activeBullets.add(bullet);
    }

    public void handleSnipe(ExoBullet bullet) {
        Vector2 delta =
                new Vector2(
                        bullet.velocityXComponent - bullet.x, bullet.velocityYComponent - bullet.y);
        delta.setLength(1000f);

        ExoUserData raycastData =
                bulletRaycast(
                        bullet.x,
                        bullet.y,
                        bullet.x + delta.x,
                        bullet.y + delta.y,
                        bullet.player.user.getPlayerId());
        if (raycastData != null) {
            if (raycastData.id != 0) {
                ((ExoPlayer) room.getPlayersList().get(raycastData.id - 1).getProperty("ExoPlayer"))
                        .hit(bullet, raycastData.part, room);
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
            ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");
            if (player != null) {
                player.draw(g, map);
            }
        }

        g.setColor(Color.ORANGE);
        for (ExoBullet bullet : activeBullets) {
            bullet.draw(g, map);
        }

        g.translate(-(int) map.translate.x, -(int) map.translate.y);
    }

    public void tick() {
        long nano = System.nanoTime();
        float deltaTime = (nano - lastNano) / 1_000_000_000f;
        lastNano = nano;

        for (User user : room.getPlayersList()) {
            ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");
            player.tick(room);
        }

        for (ExoItem item : items) {
            item.tick();
        }

        simulate(deltaTime);
    }

    public void simulate(float deltaTime) {
        List<ExoBullet> expiringBullets = new ArrayList<>();
        HashMap<ExoBullet, ExoUserData> playerHits = new HashMap<>();

        for (User user : room.getPlayersList()) {
            ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");
            if (player != null) {
                synchronized (player) {
                    player.body.setTransform(new Vector2(player.getX(), player.getY()), 0f);
                }
            }
        }

        for (ExoBullet bullet : activeBullets) {
            boolean last = false;

            float raycastDist = bullet.velocity * deltaTime;
            float remainingDist = bullet.range - bullet.dist;

            if (raycastDist >= remainingDist) {
                last = true;
                raycastDist = remainingDist;
            }

            float raycastX = bullet.velocityXComponent * raycastDist;
            float raycastY = bullet.velocityYComponent * raycastDist;

            ExoUserData raycastData =
                    bulletRaycast(
                            bullet.x,
                            bullet.y,
                            bullet.x + raycastX,
                            bullet.y + raycastY,
                            bullet.player.user.getPlayerId());

            if (raycastData != null) {
                expiringBullets.add(bullet);
                if (raycastData.id != 0) {
                    playerHits.put(bullet, raycastData);
                }
            } else {
                if (last) {
                    expiringBullets.add(bullet);
                } else {
                    synchronized (bullet) {
                        bullet.dist += raycastDist;
                        bullet.x += raycastX;
                        bullet.y += raycastY;
                    }
                }
            }
        }

        activeBullets.removeAll(expiringBullets);
        for (ExoBullet bullet : playerHits.keySet()) {
            ExoUserData hitData = playerHits.get(bullet);
            ((ExoPlayer) room.getPlayersList().get(hitData.id - 1).getProperty("ExoPlayer"))
                    .hit(bullet, hitData.part, room);
        }
    }

    private ExoUserData bulletRaycast(
            float startX, float startY, float endX, float endY, int shooterID) {
        final class RaycastHandler implements RayCastCallback {
            private ExoUserData result = null;
            private float raycastFraction = 2f;
            private boolean backcast = false;

            @Override
            public float reportRayFixture(
                    Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (backcast) {
                    float currentFraction = 1f - fraction;
                    if (currentFraction < raycastFraction) {
                        ExoUserData exoUserData = (ExoUserData) fixture.getUserData();
                        if (exoUserData.id != shooterID) {
                            result = exoUserData;
                            raycastFraction = currentFraction;
                        }
                    }
                } else {
                    ExoUserData exoUserData = (ExoUserData) fixture.getUserData();
                    if (exoUserData.id != shooterID) {
                        result = exoUserData;
                        raycastFraction = fraction;
                        return fraction;
                    }
                }
                return 1f;
            }
        }

        RaycastHandler raycastHandler = new RaycastHandler();

        raycastHandler.backcast = false;
        world.rayCast(raycastHandler, startX, startY, endX, endY);

        raycastHandler.backcast = true;
        world.rayCast(raycastHandler, endX, endY, startX, startY);

        return raycastHandler.result;
    }

    private float pickupSpawnerRaycast(float startX, float startY) {
        final class RaycastHandler implements RayCastCallback {
            private float raycastFraction = 1f;
            private boolean backcast = false;

            @Override
            public float reportRayFixture(
                    Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (backcast) {
                    float currentFraction = 1f - fraction;
                    if (currentFraction < raycastFraction) {
                        ExoUserData exoUserData = (ExoUserData) fixture.getUserData();
                        if (exoUserData.id == 0) {
                            raycastFraction = currentFraction;
                        }
                    }
                } else {
                    ExoUserData exoUserData = (ExoUserData) fixture.getUserData();
                    if (exoUserData.id == 0) {
                        raycastFraction = fraction;
                        return fraction;
                    }
                }
                return 1f;
            }
        }

        RaycastHandler raycastHandler = new RaycastHandler();

        raycastHandler.backcast = false;
        world.rayCast(raycastHandler, startX, startY, startX, startY - 12f);

        raycastHandler.backcast = true;
        world.rayCast(raycastHandler, startX, startY - 12f, startX, startY);

        return raycastHandler.raycastFraction * 12f;
    }
}
