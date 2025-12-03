package xyz.openexonaut.extension.exolib.physics;

import xyz.openexonaut.extension.exolib.game.*;

public class ExoUserData {
    public final ExoPlayer player;
    public final ExoBodyPart part;

    public ExoUserData(ExoPlayer player, ExoBodyPart part) {
        this.player = player;
        this.part = part;
    }
}
