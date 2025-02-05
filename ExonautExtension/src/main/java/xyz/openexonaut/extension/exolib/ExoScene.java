package xyz.openexonaut.extension.exolib;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import physx.*;
import physx.character.*;
import physx.common.*;
import physx.geometry.*;
import physx.physics.*;

public class ExoScene {
    private final ExoPhysics physics;
    public final ExoMap map;
    private final ExoPlayer[] players;

    private final PxScene scene;
    private final PxControllerManager controllerManager;
    public final PxRigidStatic mapCollision;

    private final PxExtendedVec3 reusableExtendedVec3 = new PxExtendedVec3(0.0, 0.0, 0.0);
    private final PxVec3 originVec = new PxVec3(0f, 0f, 0f);
    private final PxVec3 directionVec = new PxVec3(0f, 0f, 0f);

    // this "efficiently" handles concurrency automagically; the copy-on-write arraylist is probably
    // slower given the volatility
    private final Set<ExoBullet> activeBullets = ConcurrentHashMap.newKeySet();

    // private final PxRaycastBuffer10 raycastCallback = new PxRaycastBuffer10();
    private final PxRaycastHit wallRaycastHit = new PxRaycastHit();
    private final PxRaycastHit[] playerRaycastHits = new PxRaycastHit[8];
    private final boolean[] hitPlayer = new boolean[8];
    private final PxHitFlags raycastFlags = new PxHitFlags((short)(PxHitFlagEnum.eDEFAULT.value | PxHitFlagEnum.eMESH_BOTH_SIDES.value));

    public ExoScene (ExoPhysics physics, ExoMap map, ExoPlayer[] players) {
        this.physics = physics;
        this.map = map;
        this.players = players;

        scene = physics.createEmptyScene();
        mapCollision = physics.createStaticBody(map.triangleMesh);
        mapCollision.setUserData(new JavaNativeRef<>((int)0));
        scene.addActor(mapCollision);

        controllerManager = PxTopLevelFunctions.CreateControllerManager(scene);

        for (int i = 0; i < playerRaycastHits.length; i++) {
            playerRaycastHits[i] = new PxRaycastHit();
            hitPlayer[i] = false;
        }
    }

    public void spawnPlayer (int id) {
        PxController controller = physics.createController(controllerManager);
        ExoPlayer player = players[id - 1];
        controller.setUserData(new JavaNativeRef<>(id));
        reusableExtendedVec3.setZ(0);

        synchronized (player) {
            reusableExtendedVec3.setX(player.x);
            reusableExtendedVec3.setY(player.y);
            controller.setFootPosition(reusableExtendedVec3);
            player.controller = controller;
        }
    }

    public boolean spawnBullet (ExoBullet bullet) {
        return activeBullets.add(bullet);
    }

    public void draw(Graphics g) {
        g.drawImage(map.image, 0, 0, null);

        g.translate((int) map.translate.x, (int) map.translate.y);

        for (int i = 0; i < players.length; i++) {
            ExoPlayer player = players[i];
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

    public void simulate(float deltaTime) {
        List<ExoBullet> expiringBullets = new ArrayList<>();
        HashMap<ExoBullet, ExoPlayer> playerHits = new HashMap<>();

        originVec.setZ(0f);
        directionVec.setZ(0f);

        reusableExtendedVec3.setZ(0);
        for (ExoPlayer player : players) {
            if (player != null) {
                synchronized (player) {
                    reusableExtendedVec3.setX(player.x);
                    reusableExtendedVec3.setY(player.y);
                    player.controller.setFootPosition(reusableExtendedVec3);
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

            originVec.setX(bullet.x);
            originVec.setY(bullet.y);
            directionVec.setX(bullet.normalizedVelocityX * raycastDist);
            directionVec.setY(bullet.normalizedVelocityY * raycastDist);

            boolean hitWall = PxGeometryQuery.raycast(originVec, directionVec, map.triangleMesh, mapCollision.getGlobalPose(), raycastDist, raycastFlags, 1, wallRaycastHit) > 0;

            if (hitWall) {
                expiringBullets.add(bullet);
            }
            else {
                if (last) {
                    expiringBullets.add(bullet);
                }
                else {
                    bullet.x += bullet.normalizedVelocityX * raycastDist;
                    bullet.y += bullet.normalizedVelocityY * raycastDist;
                    bullet.dist += raycastDist;
                }
            }

            /*
            if (scene.raycast(originVec, directionVec, raycastDist, raycastCallback)) {
                expiringBullets.add(bullet);
                JavaNativeRef<Integer> idRef = JavaNativeRef.fromNativeObject(raycastCallback.getBlock().getActor().getUserData());
                int id = idRef.get();
                if (id != 0) {
                    playerHits.put(bullet, players[id - 1]);
                }
            }
            else {
                if (raycastCallback.hasAnyHits()) {
                    System.out.println("hit something.");
                }
                if (last) {
                    expiringBullets.add(bullet);
                }
                else {
                    bullet.x += bullet.normalizedVelocityX * raycastDist;
                    bullet.y += bullet.normalizedVelocityY * raycastDist;
                    bullet.dist += raycastDist;
                }
            }
            */
        }

        activeBullets.removeAll(expiringBullets);
        for (ExoBullet bullet : playerHits.keySet()) {
            playerHits.get(bullet).hit(bullet);
        }
    }
}
