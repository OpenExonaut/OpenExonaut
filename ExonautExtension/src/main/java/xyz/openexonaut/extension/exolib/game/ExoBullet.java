package xyz.openexonaut.extension.exolib.game;

import java.awt.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.geo.*;
import xyz.openexonaut.extension.exolib.map.*;

public class ExoBullet {
    public final int num;
    public final float range;
    public final float velocity;
    public final float velocityXComponent;
    public final float velocityYComponent;
    public final float damage;
    public final ExoPlayer player;
    public final int weaponId;
    public final boolean boosted;

    public float x;
    public float y;
    public float dist;

    // actual bullet
    public ExoBullet(
            int num,
            float range,
            float velocity,
            float angle,
            float damage,
            float x,
            float y,
            ExoPlayer player,
            ExoProps exoProps) {
        float damageModifier = getDamageModifier(player, exoProps);
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
            float startX,
            float startY,
            float endX,
            float endY,
            float damage,
            ExoPlayer player,
            ExoProps exoProps) {
        float damageModifier = getDamageModifier(player, exoProps);

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

    public void draw(Graphics g, ExoMap map) {
        ExoInt2DVector drawBullet = new Exo2DVector(x, y).convertNativeToDraw(map.scale);
        g.fillRect(drawBullet.x - 1, drawBullet.y - 1, 3, 3);
    }

    private static float getDamageModifier(ExoPlayer player, ExoProps exoProps) {
        float modifier = 1f;
        if (player.getBoost() == ExoPickupEnum.boost_damage.id) {
            modifier += exoProps.boostDamageMod;
        }
        if (player.getTeamBoost() == ExoPickupEnum.boost_team_damage.id) {
            modifier += exoProps.boostTeamDamageMod;
        }
        return modifier;
    }
}
