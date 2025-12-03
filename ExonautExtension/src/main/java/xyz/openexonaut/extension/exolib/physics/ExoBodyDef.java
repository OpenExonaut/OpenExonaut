package xyz.openexonaut.extension.exolib.physics;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoBodyDef {
    public final boolean dynamic;
    public Exo2DVector position = Exo2DVector.ZERO;

    public ExoBodyDef(boolean dynamic) {
        this.dynamic = dynamic;
    }
}
