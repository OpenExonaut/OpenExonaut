package xyz.openexonaut.extension.exolib.game;

import java.awt.*;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.map.*;

public class ExoItem extends ExoTickable {
    public final ExoItemSpawner spawner;

    private float timeToRespawn = 0f;

    public ExoItem(ExoItemSpawner spawner) {
        this.spawner = spawner;
    }

    public float timeToRespawn() {
        return timeToRespawn;
    }

    public void grabbed(ISFSArray eventQueue, Room room) {
        tick(eventQueue, room); // clock starts now, not when the last tick happened
        timeToRespawn = spawner.respawnTime;
    }

    @Override
    public float tick(ISFSArray eventQueue, Room room) {
        float deltaTime = super.tick(eventQueue, room);

        timeToRespawn = Math.max(timeToRespawn - deltaTime, 0f);

        return deltaTime;
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
