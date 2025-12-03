package xyz.openexonaut.extension.exolib.resources;

import xyz.openexonaut.extension.exolib.geo.*;
import xyz.openexonaut.extension.exolib.physics.*;

public final class ExoDefs {
    private ExoDefs() {}

    public static final float standingHalfHeight = 5f;
    public static final float crouchRollHalfHeight = 2.5f;
    public static final float radius = 1.5f;

    public static final ExoBodyDef wallDef = new ExoBodyDef(false);
    public static final ExoBodyDef playerDef = new ExoBodyDef(true);

    public static final ExoFixtureDef standingHeadDef =
            new ExoFixtureDef(
                    new ExoCircle(new Exo2DVector(0f, standingHalfHeight * 2f + radius), radius));
    public static final ExoFixtureDef standingBodyDef =
            new ExoFixtureDef(
                    new ExoAABB(
                            new Exo2DVector(0f, standingHalfHeight + radius),
                            radius,
                            standingHalfHeight));
    public static final ExoFixtureDef feetDef =
            new ExoFixtureDef(new ExoCircle(new Exo2DVector(0f, radius), radius));

    public static final ExoFixtureDef crouchRollHeadDef =
            new ExoFixtureDef(
                    new ExoCircle(new Exo2DVector(0f, crouchRollHalfHeight * 2f + radius), radius));
    public static final ExoFixtureDef crouchRollBodyDef =
            new ExoFixtureDef(
                    new ExoAABB(
                            new Exo2DVector(0f, crouchRollHalfHeight + radius),
                            radius,
                            crouchRollHalfHeight));
}
