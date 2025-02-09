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
        this.dist = 0;
    }

    public void draw(Graphics g, ExoMap map) {
        ExoInt2DVector drawBullet = new Exo2DVector(x, y).convertNativeToDraw(map.scale);
        g.fillRect(drawBullet.x - 1, drawBullet.y - 1, 3, 3);
    }
}
