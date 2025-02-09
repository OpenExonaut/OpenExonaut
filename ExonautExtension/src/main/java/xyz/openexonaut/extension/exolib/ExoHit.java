package xyz.openexonaut.extension.exolib;

public class ExoHit {
    public final ExoPlayer player;
    public final int where;

    public ExoHit(ExoPlayer player, int where) {
        this.player = player;
        this.where = where;
    }
}
