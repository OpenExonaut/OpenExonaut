package xyz.openexonaut.extension.exolib;

public class ExoBullet {
    public static final int raycastCount = 20;
    public static final float raycastFloat = (float) raycastCount;

    public final int num;
    public final float range;
    public final float velocity;
    public final float velocityX;
    public final float velocityY;
    public final int damage;
    public final ExoPlayer player;

    public float x;
    public float y;
    public float dist;

    public ExoBullet(
            int num,
            float range,
            float velocity,
            float angle,
            int damage,
            float x,
            float y,
            ExoPlayer player) {
        this.num = num;
        this.range = range;
        angle = (float) Math.toRadians(angle + 90f);
        this.velocity = velocity / raycastFloat;
        this.velocityX = (float) Math.cos(angle) * velocity / raycastFloat;
        this.velocityY = (float) Math.sin(angle) * velocity / raycastFloat;
        this.damage = damage;
        this.player = player;

        this.x = x;
        this.y = y;
        this.dist = 0;
    }
}
