package xyz.openexonaut.extension.exolib.map;

import java.awt.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class ExoItemSpawner {
    public static final float radius = 4f;

    public final ExoPickupEnum type;
    public final float respawnTime;
    public final float x;
    public final float y;

    private boolean finalized = false;
    private float height = 24f;

    public ExoItemSpawner(ExoPickupEnum type, float respawnTime, float x, float y) {
        this.type = type;
        this.respawnTime = respawnTime;
        this.x = x;
        this.y = y;
    }

    public float getHeight() {
        return height;
    }

    public void draw(Graphics g, float scale) {
        Color pickupColor = Color.BLACK;
        switch (type) {
            case boost_armor:
            case boost_damage:
            case boost_invis:
            case boost_speed:
                pickupColor = Color.RED;
                break;
            case boost_random:
                pickupColor = Color.LIGHT_GRAY;
                break;
            case boost_team_armor:
            case boost_team_damage:
            case boost_team_invis:
            case boost_team_speed:
                pickupColor = Color.BLUE;
                break;
            case pickup_grenades:
                pickupColor = Color.GREEN;
                break;
            case pickup_sniper:
            case pickup_lobber:
            case pickup_rockets:
                pickupColor = Color.GRAY;
                break;
            default:
                break;
        }
        ExoDrawUtils.fillCapsule(
                g, pickupColor, pickupColor, pickupColor, x, y, radius, height, scale);
    }

    public void finalize(float offGround) {
        if (!finalized) {
            height = offGround * 2f;
            finalized = true;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "%s spawner at (%f, %f), respawns after %f seconds.", type, x, y, respawnTime);
    }
}
