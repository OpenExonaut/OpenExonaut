package xyz.openexonaut.extension.exolib;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;

public class ExoWorld implements RayCastCallback {
    public final ExoMap map;
    private final ExoPlayer[] players;

    private final World world = new World(new Vector2(0, -100), true);

    private final FixtureDef playerHeadDef = new FixtureDef();
    private final FixtureDef playerBodyDef = new FixtureDef();
    private final FixtureDef playerFeetDef = new FixtureDef();

    private final BodyDef wallDef = new BodyDef();
    private final BodyDef playerDef = new BodyDef();

    private final Body walls;

    private Fixture raycastFixture = null;

    // this "efficiently" handles concurrency automagically; the copy-on-write arraylist is probably
    // slower given the volatility
    private final Set<ExoBullet> activeBullets = ConcurrentHashMap.newKeySet();

    public ExoWorld(ExoMap map, ExoPlayer[] players) {
        this.map = map;
        this.players = players;

        wallDef.awake = false;
        playerDef.fixedRotation = true;

        walls = world.createBody(wallDef);
        for (FixtureDef wall : map.wallFixtures) {
            walls.createFixture(wall);
        }
        walls.setUserData(new ExoUserData(0, 0));

        CircleShape head = new CircleShape();
        head.setPosition(new Vector2(0f, 11.5f));
        head.setRadius(1.5f);

        PolygonShape body = new PolygonShape();
        body.setAsBox(1.5f, 5f, new Vector2(0f, 6.5f), 0);

        CircleShape feet = new CircleShape();
        feet.setPosition(new Vector2(0f, 1.5f));
        feet.setRadius(1.5f);

        playerHeadDef.shape = head;
        playerBodyDef.shape = body;
        playerFeetDef.shape = feet;
    }

    public void spawnPlayer(int id) {
        ExoPlayer player = players[id - 1];
        playerDef.position.set(player.x, player.y);
        player.body = world.createBody(playerDef);
        player.body.createFixture(playerHeadDef).setUserData(new ExoUserData(id, 1));
        player.body.createFixture(playerBodyDef).setUserData(new ExoUserData(id, 2));
        player.body.createFixture(playerFeetDef).setUserData(new ExoUserData(id, 3));
    }

    public boolean spawnBullet(ExoBullet bullet) {
        return activeBullets.add(bullet);
    }

    public void draw(Graphics g) {
        map.draw(g);

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
        HashMap<ExoBullet, ExoHit> playerHits = new HashMap<>();

        for (ExoPlayer player : players) {
            if (player != null) {
                synchronized (player) {
                    player.body.setTransform(new Vector2(player.x, player.y), 0);
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

            raycastFixture = null;
            world.rayCast(this, bullet.x, bullet.y, bullet.x + raycastX, bullet.y + raycastY);

            // raycasts that start inside a fixture ignore that fixture
            if (raycastFixture == null) {
                world.rayCast(this, bullet.x + raycastX, bullet.y + raycastY, bullet.x, bullet.y);
            }

            if (raycastFixture != null) {
                expiringBullets.add(bullet);
                if (raycastFixture.getUserData() != null) {
                    if (raycastFixture.getUserData() instanceof ExoUserData) {
                        ExoUserData userData = (ExoUserData) raycastFixture.getUserData();
                        if (userData.id != 0) {
                            playerHits.put(
                                    bullet, new ExoHit(players[userData.id - 1], userData.part));
                        }
                    }
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
            ExoHit hit = playerHits.get(bullet);
            hit.player.hit(bullet, hit.where);
        }
    }

    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        raycastFixture = fixture;
        return fraction;
    }
}
