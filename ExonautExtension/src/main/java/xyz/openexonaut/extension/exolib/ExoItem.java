package xyz.openexonaut.extension.exolib;

import java.awt.*;

public class ExoItem {
    public final ExoItemSpawner spawner;
    private float timeToRespawn = 0f;
    private long lastNano = System.nanoTime();

    public ExoItem(ExoItemSpawner spawner) {
        this.spawner = spawner;
    }

    public float timeToRespawn() {
        return timeToRespawn;
    }

    public void grabbed() {
        tick(); // clock starts now, not when the last tick happened
        timeToRespawn = spawner.respawnTime;
    }

    public void tick() {
        long nano = System.nanoTime();
        float deltaTime = (nano - lastNano) / 1_000_000_000f;
        lastNano = nano;

        if (timeToRespawn > 0f) {
            timeToRespawn = Math.max(timeToRespawn - deltaTime, 0f);
        }
    }

    public boolean active() {
        return timeToRespawn == 0f;
    }

    public void draw(Graphics g, float scale) {
        if (active()) {
            spawner.draw(g, scale);
        }
    }
}
