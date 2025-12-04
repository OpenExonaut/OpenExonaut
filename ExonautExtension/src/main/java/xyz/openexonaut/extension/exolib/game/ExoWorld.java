package xyz.openexonaut.extension.exolib.game;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.evthandlers.*;
import xyz.openexonaut.extension.exolib.geo.*;
import xyz.openexonaut.extension.exolib.map.*;
import xyz.openexonaut.extension.exolib.physics.*;
import xyz.openexonaut.extension.exolib.resources.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class ExoWorld extends ExoTickable {
    private static final ExoUserData wallUserData = new ExoUserData(null, null);
    private static final Exo2DVector gravity = new Exo2DVector(0f, -100f);

    public final ExoMap map;
    public final ExoItem[] items;
    public final boolean team;

    private final Room room;

    private ExoSim world = new ExoSim(gravity, true);

    private final Map<Integer, ExoBullet> activeBullets = new ConcurrentHashMap<>();
    private final Map<Integer, ExoBullet> inactiveRockets = new ConcurrentHashMap<>();
    private final Map<Integer, ExoGrenade> activeGrenades = new ConcurrentHashMap<>();

    private final List<ExoPlayer> banzaiMembers = new ArrayList<>(8);
    private final List<ExoPlayer> atlasMembers = new ArrayList<>(8);

    public ExoWorld(ExoMap map, Room room) {
        this.map = map;
        this.room = room;

        team = room.getVariable("mode").getStringValue().equals("team");

        ExoBody walls = world.createBody(ExoDefs.wallDef);
        for (ExoFixtureDef wall : map.wallFixtureDefs) {
            walls.createFixture(wall, wallUserData);
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

        if (team) {
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

    public void spawnPlayer(User user) {
        ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");
        ExoDefs.playerDef.position = new Exo2DVector(player.getX(), player.getY());

        ExoUserData headUserData = new ExoUserData(player, ExoBodyPart.HEAD);
        ExoUserData bodyUserData = new ExoUserData(player, ExoBodyPart.BODY);
        ExoUserData feetUserData = new ExoUserData(player, ExoBodyPart.FEET);

        ExoBody crouchingBody = world.createBody(ExoDefs.playerDef);
        crouchingBody.createFixture(ExoDefs.crouchRollHeadDef, headUserData);
        crouchingBody.createFixture(ExoDefs.crouchRollBodyDef, bodyUserData);
        crouchingBody.createFixture(ExoDefs.feetDef, feetUserData);
        crouchingBody.active = false;

        ExoBody standingBody = world.createBody(ExoDefs.playerDef);
        standingBody.createFixture(ExoDefs.standingHeadDef, headUserData);
        standingBody.createFixture(ExoDefs.standingBodyDef, bodyUserData);
        standingBody.createFixture(ExoDefs.feetDef, feetUserData);

        player.setBodies(standingBody, crouchingBody);

        (user.getVariable("faction").getStringValue().equals("banzai")
                        ? banzaiMembers
                        : atlasMembers)
                .add(player);
    }

    public void removePlayer(User user) {
        ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");

        world.destroyBody(player.getStandingBody());
        world.destroyBody(player.getCrouchingBody());

        List<Integer> playerBullets = new ArrayList<>();
        List<Integer> playerRockets = new ArrayList<>();
        List<Integer> playerGrenades = new ArrayList<>();

        for (ExoBullet bullet : activeBullets.values()) {
            if (bullet.player == player) playerBullets.add(bullet.num);
        }
        for (ExoBullet bullet : inactiveRockets.values()) {
            if (bullet.player == player) playerRockets.add(bullet.num);
        }
        for (ExoGrenade grenade : activeGrenades.values()) {
            if (grenade.player == player) playerGrenades.add(grenade.num);
        }

        for (int num : playerBullets) {
            activeBullets.remove(num);
        }
        for (int num : playerRockets) {
            inactiveRockets.remove(num);
        }
        for (int num : playerGrenades) {
            activeGrenades.remove(num);
        }

        banzaiMembers.remove(player);
        atlasMembers.remove(player);
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

        Exo2DVector delta =
                new Exo2DVector(bullet.velocityXComponent - x, bullet.velocityYComponent - y)
                        .withMagnitude(1000f);

        ExoUserData raycastData =
                bulletRaycast(x, y, x + delta.x, y + delta.y, bullet.player, true);
        if (raycastData != null) {
            if (raycastData.player != null) {
                ISFSArray eventArray = new SFSArray();
                raycastData.player.bulletHit(bullet, raycastData.part, room, eventArray);
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

    private void processBlastFixtures(
            Set<ExoPlayer> currentSet,
            Set<ExoPlayer> otherSet,
            ExoFixture fixture,
            float blastX,
            float blastY) {
        ExoPlayer player = fixture.userData.player;

        if (player == null || currentSet.contains(player)) return;
        if (otherSet.contains(player)) {
            currentSet.add(player);
        }

        boolean blocked = true;
        for (ExoFixture bodyPart : player.getBody().getFixtureList()) {
            Exo2DVector bodyPartCenter = player.getBody().getPosition();

            switch (bodyPart.userData.part) {
                case HEAD:
                    bodyPartCenter =
                            bodyPartCenter.plus(
                                    0f,
                                    ExoDefs.radius
                                            + 2f
                                                    * (player.isSmall()
                                                            ? ExoDefs.crouchRollHalfHeight
                                                            : ExoDefs.standingHalfHeight));
                    break;
                case BODY:
                    bodyPartCenter =
                            bodyPartCenter.plus(
                                    0f,
                                    ExoDefs.radius
                                            + (player.isSmall()
                                                    ? ExoDefs.crouchRollHalfHeight
                                                    : ExoDefs.standingHalfHeight));
                    break;
                case FEET:
                    bodyPartCenter = bodyPartCenter.plus(0f, ExoDefs.radius);
                    break;
            }

            ExoRaycastResult result =
                    world.rayCast(
                            true,
                            true,
                            exoUserData -> exoUserData.player == player,
                            bodyPartCenter.x,
                            bodyPartCenter.y,
                            blastX,
                            blastY);

            if (result.fixture != null) {
                if (result.fixture.userData.player == player) {
                    blocked = false;
                    break;
                }
            }
        }

        if (!blocked) {
            currentSet.add(player);
        }
    }

    private Predicate<ExoUserData> getWeaponTest(ExoPlayer attacker) {
        if (team) {
            List<ExoPlayer> teammateIDs =
                    banzaiMembers.contains(attacker) ? banzaiMembers : atlasMembers;
            return exoUserData -> !teammateIDs.contains(exoUserData.player);
        } else {
            return exoUserData -> exoUserData.player != attacker;
        }
    }

    private void explosion(
            ExoPlayer creator,
            int weaponId,
            float damageModifier,
            float x,
            float y,
            ISFSArray eventQueue) {
        ExoWeapon weapon = ExoGameData.getWeapon(weaponId);

        Predicate<ExoUserData> test = getWeaponTest(creator);

        List<ExoFixture> blast1Fixtures = world.circleTest(x, y, weapon.Radius1, test);
        List<ExoFixture> blast2Fixtures = world.circleTest(x, y, weapon.Radius2, test);

        Set<ExoPlayer> blast1Set = new HashSet<>(8);
        Set<ExoPlayer> blast2Set = new HashSet<>(8);

        for (ExoFixture fixture : blast1Fixtures) {
            processBlastFixtures(blast1Set, blast2Set, fixture, x, y);
        }
        for (ExoFixture fixture : blast2Fixtures) {
            processBlastFixtures(blast2Set, blast1Set, fixture, x, y);
        }

        // TODO: are the blast radii really mutually exclusive?
        blast2Fixtures.removeAll(blast1Fixtures);

        for (ExoPlayer damaged : blast1Set) {
            damaged.blastHit(
                    creator,
                    weaponId,
                    weapon.Radius1_Damage,
                    damageModifier,
                    false,
                    room,
                    eventQueue);
        }
        for (ExoPlayer damaged : blast2Set) {
            damaged.blastHit(
                    creator,
                    weaponId,
                    weapon.Radius2_Damage,
                    damageModifier,
                    false,
                    room,
                    eventQueue);
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
        for (ExoBullet bullet : activeBullets.values()) {
            bullet.draw(g, map);
        }

        g.translate(-(int) map.translate.x, -(int) map.translate.y);
    }

    @Override
    public float tick(ISFSArray eventQueue, Room room) {
        float deltaTime = super.tick(eventQueue, room);

        // TODO: does this adequately address the player tunneling problem?
        simulateBullets(eventQueue, false, true);

        for (User user : room.getPlayersList()) {
            if (user != null) {
                ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");
                player.tick(eventQueue, room);
            }
        }

        // bullet didn't move and walls didn't move, so don't check their intercollisions
        simulateBullets(eventQueue, true, false);

        for (ExoItem item : items) {
            item.tick(eventQueue, room);
        }

        return deltaTime;
    }

    private void simulateBullets(
            ISFSArray eventQueue, boolean commitDistance, boolean includeStatic) {
        List<ExoBullet> expiringBullets = new ArrayList<>(activeBullets.size());
        Map<ExoBullet, ExoUserData> playerHits = new HashMap<>();

        for (ExoBullet bullet : activeBullets.values()) {
            boolean last = false;

            float raycastDist =
                    bullet.velocity
                            * (commitDistance ? bullet.tick(eventQueue, room) : bullet.time());
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
                    bulletRaycast(x, y, x + raycastX, y + raycastY, bullet.player, includeStatic);

            if (raycastData != null) {
                expiringBullets.add(bullet);
                if (raycastData.player != null) {
                    playerHits.put(bullet, raycastData);

                    if (bullet.weaponId == 8) {
                        eventQueue.addSFSObject(
                                ExoParamUtils.serialize(
                                        new SendRocketExplode(x, y, bullet.num),
                                        bullet.player.user.getPlayerId(room)));
                    }
                } else if (bullet.weaponId == 8) {
                    inactiveRockets.put(bullet.num, bullet);
                }
            } else if (commitDistance) {
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
            hitData.player.bulletHit(bullet, hitData.part, room, eventQueue);
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
            float startX,
            float startY,
            float endX,
            float endY,
            ExoPlayer shooter,
            boolean includeStatic) {
        ExoRaycastResult result =
                world.rayCast(
                        includeStatic, true, getWeaponTest(shooter), startX, startY, endX, endY);

        if (result.fixture != null) {
            return result.fixture.userData;
        }
        return null;
    }

    private float pickupSpawnerRaycast(float startX, float startY) {
        ExoRaycastResult result =
                world.rayCast(
                        true,
                        false,
                        a -> false,
                        startX,
                        startY,
                        startX,
                        startY - ExoItemSpawner.halfMaxHeight);

        return result.dist;
    }
}
