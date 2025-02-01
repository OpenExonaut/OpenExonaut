package xyz.openexonaut.extension.exolib;

public class ExoItemSpawner {
    public final int type;
    public final float respawnTime;
    public final float x;
    public final float y;

    public ExoItemSpawner (int type, float respawnTime, float x, float y) {
        this.type = type;
        this.respawnTime = respawnTime;
        this.x = x;
        this.y = y;
    }

    public ExoItemSpawner convertNativeToDraw (float scalar) {
        return new ExoItemSpawner(type, respawnTime, x * scalar, y * -scalar);
    }

    @Override public String toString () {
        return "(" + x + ", " + y + ")";
    }
}
