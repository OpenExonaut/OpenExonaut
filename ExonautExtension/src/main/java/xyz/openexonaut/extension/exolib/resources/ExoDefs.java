package xyz.openexonaut.extension.exolib.resources;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import xyz.openexonaut.extension.exolib.data.*;

public final class ExoDefs {
    private ExoDefs() {}

    public static final float standingHalfHeight = 5f;
    public static final float crouchRollHalfHeight = 2.5f;
    public static final float radius = 1.5f;

    public static final BodyDef wallDef = new BodyDef();
    public static final BodyDef playerDef = new BodyDef();
    public static final BodyDef blastDef = new BodyDef();

    public static final FixtureDef standingHeadDef = new FixtureDef();
    public static final FixtureDef standingBodyDef = new FixtureDef();
    public static final FixtureDef standingFeetDef = new FixtureDef();

    public static final FixtureDef crouchRollHeadDef = new FixtureDef();
    public static final FixtureDef crouchRollBodyDef = new FixtureDef();
    public static final FixtureDef crouchRollFeetDef = new FixtureDef();

    public static final FixtureDef rocketBlastDef1 = new FixtureDef();
    public static final FixtureDef rocketBlastDef2 = new FixtureDef();
    public static final FixtureDef lobberBlastDef1 = new FixtureDef();
    public static final FixtureDef lobberBlastDef2 = new FixtureDef();
    public static final FixtureDef grenadeBlastDef1 = new FixtureDef();
    public static final FixtureDef grenadeBlastDef2 = new FixtureDef();

    static {
        Box2D.init();

        wallDef.awake = false;
        playerDef.fixedRotation = true;
        blastDef.type =
                BodyType.DynamicBody; // kinematic bodies don't interact with kinematic or static
        // bodies
        blastDef.gravityScale = 0f;

        rocketBlastDef1.isSensor = true;
        rocketBlastDef2.isSensor = true;
        lobberBlastDef1.isSensor = true;
        lobberBlastDef2.isSensor = true;
        grenadeBlastDef1.isSensor = true;
        grenadeBlastDef2.isSensor = true;
    }

    public static void init() {
        destroy();

        createShapes(standingHeadDef, standingBodyDef, standingFeetDef, standingHalfHeight);
        createShapes(crouchRollHeadDef, crouchRollBodyDef, crouchRollFeetDef, crouchRollHalfHeight);

        initBlastDefs(rocketBlastDef1, rocketBlastDef2, 8);
        initBlastDefs(lobberBlastDef1, lobberBlastDef2, 6);
        initBlastDefs(grenadeBlastDef1, grenadeBlastDef2, 9);
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

    private static void initBlastDefs(FixtureDef blast1Def, FixtureDef blast2Def, int weaponId) {
        ExoWeapon weapon = ExoGameData.getWeapon(weaponId);

        CircleShape blast = new CircleShape();
        blast.setPosition(Vector2.Zero);
        blast.setRadius(weapon.Radius1);
        blast1Def.shape = blast;

        blast = new CircleShape();
        blast.setPosition(Vector2.Zero);
        blast.setRadius(weapon.Radius2);
        blast2Def.shape = blast;
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
        if (rocketBlastDef1.shape != null) {
            rocketBlastDef1.shape.dispose();
            rocketBlastDef1.shape = null;
        }
        if (rocketBlastDef2.shape != null) {
            rocketBlastDef2.shape.dispose();
            rocketBlastDef2.shape = null;
        }
        if (lobberBlastDef1.shape != null) {
            lobberBlastDef1.shape.dispose();
            lobberBlastDef1.shape = null;
        }
        if (lobberBlastDef2.shape != null) {
            lobberBlastDef2.shape.dispose();
            lobberBlastDef2.shape = null;
        }
        if (grenadeBlastDef1.shape != null) {
            grenadeBlastDef1.shape.dispose();
            grenadeBlastDef1.shape = null;
        }
        if (grenadeBlastDef2.shape != null) {
            grenadeBlastDef2.shape.dispose();
            grenadeBlastDef2.shape = null;
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
