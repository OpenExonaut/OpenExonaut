package xyz.openexonaut.extension.exolib.resources;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;

public final class ExoDefs {
    private ExoDefs() {}

    public static final float standingHalfHeight = 5f;
    public static final float crouchRollHalfHeight = 2.5f;
    public static final float radius = 1.5f;

    public static final BodyDef wallDef = new BodyDef();
    public static final BodyDef playerDef = new BodyDef();

    public static final FixtureDef standingHeadDef = new FixtureDef();
    public static final FixtureDef standingBodyDef = new FixtureDef();
    public static final FixtureDef standingFeetDef = new FixtureDef();

    public static final FixtureDef crouchRollHeadDef = new FixtureDef();
    public static final FixtureDef crouchRollBodyDef = new FixtureDef();
    public static final FixtureDef crouchRollFeetDef = new FixtureDef();

    static {
        Box2D.init();

        wallDef.awake = false;
        playerDef.fixedRotation = true;
    }

    public static void init() {
        destroy();

        createShapes(standingHeadDef, standingBodyDef, standingFeetDef, standingHalfHeight);
        createShapes(crouchRollHeadDef, crouchRollBodyDef, crouchRollFeetDef, crouchRollHalfHeight);
    }

    private static void createShapes(
            FixtureDef headDef, FixtureDef bodyDef, FixtureDef feetDef, float halfHeight) {
        CircleShape head = new CircleShape();
        head.setPosition(new Vector2(0f, halfHeight * 2f + radius));
        head.setRadius(radius);

        PolygonShape body = new PolygonShape();
        body.setAsBox(radius, halfHeight, new Vector2(0f, halfHeight + radius), 0f);

        CircleShape feet = new CircleShape();
        feet.setPosition(new Vector2(0f, radius));
        feet.setRadius(radius);

        headDef.shape = head;
        bodyDef.shape = body;
        feetDef.shape = feet;
    }

    public static void destroy() {
        if (standingHeadDef.shape != null) {
            standingHeadDef.shape.dispose();
            standingHeadDef.shape = null;
        }
        if (standingBodyDef.shape != null) {
            standingBodyDef.shape.dispose();
            standingBodyDef.shape = null;
        }
        if (standingFeetDef.shape != null) {
            standingFeetDef.shape.dispose();
            standingFeetDef.shape = null;
        }
        if (crouchRollHeadDef.shape != null) {
            crouchRollHeadDef.shape.dispose();
            crouchRollHeadDef.shape = null;
        }
        if (crouchRollBodyDef.shape != null) {
            crouchRollBodyDef.shape.dispose();
            crouchRollBodyDef.shape = null;
        }
        if (crouchRollFeetDef.shape != null) {
            crouchRollFeetDef.shape.dispose();
            crouchRollFeetDef.shape = null;
        }
    }

    public static void setFilters(short category, short mask) {
        standingHeadDef.filter.categoryBits = category;
        standingBodyDef.filter.categoryBits = category;
        standingFeetDef.filter.categoryBits = category;
        crouchRollHeadDef.filter.categoryBits = category;
        crouchRollBodyDef.filter.categoryBits = category;
        crouchRollFeetDef.filter.categoryBits = category;

        standingHeadDef.filter.maskBits = mask;
        standingBodyDef.filter.maskBits = mask;
        standingFeetDef.filter.maskBits = mask;
        crouchRollHeadDef.filter.maskBits = mask;
        crouchRollBodyDef.filter.maskBits = mask;
        crouchRollFeetDef.filter.maskBits = mask;
    }
}
