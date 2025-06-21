package xyz.openexonaut.extension.exolib.game;

import java.awt.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.geo.*;
import xyz.openexonaut.extension.exolib.map.*;
import xyz.openexonaut.extension.exolib.resources.*;

public class ExoBullet extends ExoTickable {
    public final int num;
    public final float range;
    public final float velocity;
    public final float velocityXComponent;
    public final float velocityYComponent;
    public final float damage;
    public final ExoPlayer player;
    public final int weaponId;
    public final boolean boosted;

    private float x;
    private float y;
    private float dist;

    // actual bullet
    public ExoBullet(
            int num,
            float range,
            float velocity,
            float angle,
            float damage,
            float x,
            float y,
            ExoPlayer player) {
        float damageModifier = getDamageModifier(player);
        angle = (float) Math.toRadians(angle + 90f);

        this.num = num;
        this.range = range;
        this.velocity = velocity;
        this.velocityXComponent = (float) Math.cos(angle);
        this.velocityYComponent = (float) Math.sin(angle);
        this.player = player;
        this.weaponId = player.getWeaponId();

        this.x = x;
        this.y = y;
        this.dist = 0f;

        this.boosted = damageModifier > 1f;
        this.damage = damage * damageModifier;
    }

    // sniper hitscan
    public ExoBullet(
            float startX, float startY, float endX, float endY, float damage, ExoPlayer player) {
        float damageModifier = getDamageModifier(player);

        this.num = -1;
        this.range = 1000f;
        this.velocity = Float.POSITIVE_INFINITY;
        this.velocityXComponent = endX; // that's right,
        this.velocityYComponent = endY; // we're gonna cheat!
        this.player = player;
        this.weaponId = player.getWeaponId();

        this.x = startX;
        this.y = startY;
        this.dist = 0f;

        this.boosted = damageModifier > 1f;
        this.damage = damage * damageModifier;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getDist() {
        return dist;
    }

    public void addX(float x) {
        this.x += x;
    }

    public void addY(float y) {
        this.y += y;
    }

    public void addDist(float dist) {
        this.dist += dist;
    }

    public void draw(Graphics g, ExoMap map) {
        ExoInt2DVector drawBullet = new Exo2DVector(x, y).convertNativeToDraw(map.scale);
        g.fillRect(drawBullet.x - 1, drawBullet.y - 1, 3, 3);
    }

    private static float getDamageModifier(ExoPlayer player) {
        float modifier = 1f;
        if (player.getBoost() == ExoPickupEnum.boost_damage.id) {
            modifier += ExoProps.getBoostDamageMod();
        }
        if (player.getTeamBoost() == ExoPickupEnum.boost_team_damage.id) {
            modifier += ExoProps.getBoostTeamDamageMod();
        }
        return modifier;
    }
}
