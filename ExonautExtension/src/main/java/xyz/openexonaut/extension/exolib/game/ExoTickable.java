package xyz.openexonaut.extension.exolib.game;

import com.smartfoxserver.v2.entities.data.*;

public abstract class ExoTickable {
    private long lastNano = System.nanoTime();

    // return value: seconds since last tick
    public float tick(ISFSArray eventQueue) {
        long nano = System.nanoTime();
        float deltaTime = (nano - lastNano) / 1_000_000_000f;
        lastNano = nano;

        return deltaTime;
    }
}
