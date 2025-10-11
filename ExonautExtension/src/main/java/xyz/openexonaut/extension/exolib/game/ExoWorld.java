package xyz.openexonaut.extension.exolib.game;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.Shape;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.evthandlers.SendRocketExplode;
import xyz.openexonaut.extension.exolib.map.*;
import xyz.openexonaut.extension.exolib.resources.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class ExoWorld extends ExoTickable {
    public final ExoMap map;
    public final ExoItem[] items;

    private final Room room;

    private World world = new World(new Vector2(0f, -100f), true);
    private ExoContactListener contactListener = new ExoContactListener();

    private final Map<Integer, ExoBullet> activeBullets = new ConcurrentHashMap<>();
    private final Map<Integer, ExoBullet> inactiveRockets = new ConcurrentHashMap<>();
    private final Map<Integer, ExoGrenade> activeGrenades = new ConcurrentHashMap<>();

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

        world.setContactListener(contactListener);
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

    public void spawnBullet(ExoBullet bullet) {
        activeBullets.put(bullet.num, bullet);
    }

    public void spawnGrenade(ExoGrenade grenade) {
        activeGrenades.put(grenade.num, grenade);
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
                        .bulletHit(bullet, raycastData.part, eventArray);
                ExoSendUtils.sendEventArrayToAll(room, eventArray);
            }
        }
    }

    // return value: whether a primed rocket matched provided num
    public boolean explodeRocket(int num, float x, float y) {
        ExoBullet rocket = activeBullets.remove(num);
        if (rocket == null) {
            rocket = inactiveRockets.remove(num);
            if (rocket == null) {
                return false;
            }
        }

        ISFSArray eventArray = new SFSArray();
        explosion(rocket.player, rocket.weaponId, rocket.damageModifier, x, y, eventArray);
        ExoSendUtils.sendEventArrayToAll(room, eventArray);

        return true;
    }

    // return value: whether a primed grenade matched provided num
    public boolean explodeGrenade(int num, float x, float y) {
        ExoGrenade grenade = activeGrenades.remove(num);
        if (grenade == null) return false;

        ISFSArray eventArray = new SFSArray();
        explosion(grenade.player, grenade.weaponId, grenade.damageModifier, x, y, eventArray);
        ExoSendUtils.sendEventArrayToAll(room, eventArray);

        return true;
    }

    private void explosion(
            ExoPlayer player,
            int weaponId,
            float damageModifier,
            float x,
            float y,
            ISFSArray eventQueue) {
        FixtureDef blast1Def;
        FixtureDef blast2Def;
        switch (weaponId) {
            case 8:
                blast1Def = ExoDefs.rocketBlastDef1;
                blast2Def = ExoDefs.rocketBlastDef2;
                break;
            case 6:
                blast1Def = ExoDefs.lobberBlastDef1;
                blast2Def = ExoDefs.lobberBlastDef2;
                break;
            case 9:
                blast1Def = ExoDefs.grenadeBlastDef1;
                blast2Def = ExoDefs.grenadeBlastDef2;
                break;
            default:
                throw new RuntimeException(
                        String.format("Invalid explosive weapon ID %d", weaponId));
        }

        blast1Def.filter.categoryBits = ExoFilterUtils.getWeaponCategory(player.user.getPlayerId());
        blast1Def.filter.maskBits = ExoFilterUtils.getWeaponMask(player.user.getPlayerId());
        blast2Def.filter.set(blast1Def.filter);

        ExoDefs.blastDef.position.set(x, y);
        Body blastBody = world.createBody(ExoDefs.blastDef);
        Fixture blast1Fixture = blastBody.createFixture(blast1Def);
        blast1Fixture.setUserData(new ExoUserData(-1, 1));
        Fixture blast2Fixture = blastBody.createFixture(blast2Def);
        blast2Fixture.setUserData(new ExoUserData(-1, 2));

        world.step(0, 0, 0);
        world.destroyBody(blastBody);

        contactListener.blast2Set.removeAll(
                contactListener.blast1Set); // TODO: are the blast radii really mutually exclusive?

        ExoWeapon weapon = ExoGameData.getWeapon(weaponId);
        for (ExoPlayer damaged : contactListener.blast1Set) {
            damaged.blastHit(
                    player, weaponId, weapon.Radius1_Damage, damageModifier, false, eventQueue);
        }
        for (ExoPlayer damaged : contactListener.blast2Set) {
            damaged.blastHit(
                    player, weaponId, weapon.Radius2_Damage, damageModifier, false, eventQueue);
        }

        contactListener.blast1Set.clear();
        contactListener.blast2Set.clear();
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
        for (ExoBullet bullet : activeBullets.values()) {
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

    private void simulateBullets(ISFSArray eventQueue) {
        List<ExoBullet> expiringBullets = new ArrayList<>(activeBullets.size());
        Map<ExoBullet, ExoUserData> playerHits = new HashMap<>();

        for (ExoBullet bullet : activeBullets.values()) {
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

                    if (bullet.weaponId == 8) {
                        eventQueue.addSFSObject(
                                ExoParamUtils.serialize(
                                        new SendRocketExplode(x, y, bullet.num),
                                        bullet.player.user.getPlayerId()));
                    }
                } else if (bullet.weaponId == 8) {
                    inactiveRockets.put(bullet.num, bullet);
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

        for (ExoBullet expiringBullet : expiringBullets) {
            activeBullets.remove(expiringBullet.num);
        }
        for (ExoBullet bullet : playerHits.keySet()) {
            ExoUserData hitData = playerHits.get(bullet);
            ((ExoPlayer) room.getPlayersList().get(hitData.id - 1).getProperty("ExoPlayer"))
                    .bulletHit(bullet, hitData.part, eventQueue);
            if (bullet.weaponId == 8) {
                explosion(
                        bullet.player,
                        bullet.weaponId,
                        bullet.damageModifier,
                        bullet.getX(),
                        bullet.getY(),
                        eventQueue);
            }
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

    private class ExoContactListener implements ContactListener {
        public final Set<ExoPlayer> blast1Set = ConcurrentHashMap.newKeySet(8);
        public final Set<ExoPlayer> blast2Set = ConcurrentHashMap.newKeySet(8);

        @Override
        public void beginContact(Contact contact) {
            Fixture blastFixture = null;
            Fixture playerFixture = null;

            ExoUserData first = (ExoUserData) contact.getFixtureA().getUserData();
            ExoUserData second = (ExoUserData) contact.getFixtureB().getUserData();

            if (first.id == -1) blastFixture = contact.getFixtureA();
            else if (first.id > 0 && first.id <= 8) playerFixture = contact.getFixtureA();

            if (second.id == -1) blastFixture = contact.getFixtureB();
            else if (second.id > 0 && second.id <= 8) playerFixture = contact.getFixtureB();

            // explosive blast handler
            if (blastFixture != null && playerFixture != null) {
                int playerFixtureId = ((ExoUserData) playerFixture.getUserData()).id;

                ExoPlayer player =
                        (ExoPlayer)
                                room.getPlayersList()
                                        .get(playerFixtureId - 1)
                                        .getProperty("ExoPlayer");
                Set<ExoPlayer> currentSet;
                Set<ExoPlayer> otherSet;

                if (((ExoUserData) blastFixture.getUserData()).part == 1) {
                    currentSet = blast1Set;
                    otherSet = blast2Set;
                } else {
                    currentSet = blast2Set;
                    otherSet = blast1Set;
                }

                // if one isn't blocked, the other won't be since they're the same origin and
                // destinations
                if (otherSet.contains(player)) {
                    currentSet.add(player);
                } else {
                    boolean blocked = true;
                    for (Fixture bodyPart : player.getBody().getFixtureList()) {
                        ExoRaycastHandler raycastHandler =
                                new ExoRaycastHandler(
                                        exoUserData ->
                                                exoUserData.id == 0
                                                        || exoUserData.id == playerFixtureId);

                        Vector2 bodyPartCenter;
                        Shape bodyPartShape = bodyPart.getShape();
                        if (bodyPart.getShape() instanceof CircleShape) {
                            bodyPartCenter = ((CircleShape) bodyPartShape).getPosition();
                        } else {
                            bodyPartCenter = player.getBody().getPosition().cpy();
                            bodyPartCenter.add(
                                    0f,
                                    ExoDefs.radius
                                            + ExoDefs.standingHalfHeight); // TODO: crouch handling
                        }

                        world.rayCast(
                                raycastHandler,
                                bodyPartCenter.x,
                                bodyPartCenter.y,
                                ExoDefs.blastDef.position.x,
                                ExoDefs.blastDef.position.y);

                        raycastHandler.backcast = true;
                        world.rayCast(
                                raycastHandler,
                                ExoDefs.blastDef.position.x,
                                ExoDefs.blastDef.position.y,
                                bodyPartCenter.x,
                                bodyPartCenter.y);

                        if (raycastHandler.result.id == playerFixtureId) {
                            blocked = false;
                            break;
                        }
                    }

                    if (!blocked) {
                        currentSet.add(player);
                    }
                }
            }
        }

        @Override
        public void endContact(Contact contact) {}

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {}

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {}
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
