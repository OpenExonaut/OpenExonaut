package xyz.openexonaut.extension.exolib;

import java.awt.*;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoBullet {
    public final int num;
    public final float range;
    public final float velocity;
    public final float velocityXComponent;
    public final float velocityYComponent;
    public final int damage;
    public final ExoPlayer player;

    public float x;
    public float y;
    public float dist;

    // actual bullet
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
        this.velocity = velocity;
        this.velocityXComponent = (float) Math.cos(angle);
        this.velocityYComponent = (float) Math.sin(angle);
        this.damage = damage;
        this.player = player;

        this.x = x;
        this.y = y;
        this.dist = 0f;
    }

    // sniper hitscan
    public ExoBullet(
            float startX, float startY, float endX, float endY, int damage, ExoPlayer player) {
        this.num = -1;
        this.range = 1000f;
        this.velocity = Float.POSITIVE_INFINITY;
        this.velocityXComponent = endX; // that's right,
        this.velocityYComponent = endY; // we're gonna cheat!
        this.damage = damage;
        this.player = player;

        this.x = startX;
        this.y = startY;
        this.dist = 0f;
    }

    public void draw(Graphics g, ExoMap map) {
        ExoInt2DVector drawBullet = new Exo2DVector(x, y).convertNativeToDraw(map.scale);
        g.fillRect(drawBullet.x - 1, drawBullet.y - 1, 3, 3);
    }
}
