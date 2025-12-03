package xyz.openexonaut.extension.exolib.physics;

import java.util.*;
import java.util.function.*;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoSim {
    private final List<ExoBody> bodies = new ArrayList<>(9);

    public Exo2DVector gravity = Exo2DVector.ZERO;
    public boolean doSleep = true;

    public ExoSim(Exo2DVector gravity, boolean doSleep) {
        this.gravity = gravity;
        this.doSleep = doSleep;
    }

    public ExoBody createBody(ExoBodyDef bodyDef) {
        ExoBody retVal = new ExoBody(bodyDef);
        bodies.add(retVal);
        return retVal;
    }

    public void destroyBody(ExoBody body) {
        bodies.remove(body);
    }

    public ExoRaycastResult rayCast(
            boolean includeStatic,
            boolean includeDynamic,
            Predicate<ExoUserData> testPlayers,
            float startX,
            float startY,
            float endX,
            float endY) {
        float dx = endX - startX;
        float dy = endY - startY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        ExoRaycastResult result = new ExoRaycastResult(endX, endY, null, length);
        for (ExoBody body : bodies) {
            if (body.active) {
                if (body.dynamic) {
                    if (includeDynamic) {
                        result = body.rayCast(result, testPlayers, startX, startY, dx, dy, length);
                    }
                } else if (includeStatic) {
                    result = body.rayCast(result, a -> true, startX, startY, dx, dy, length);
                }
            }
            if (result.dist == 0f) break;
        }
        return result;
    }

    public List<ExoFixture> circleTest(
            float x, float y, float radius, Predicate<ExoUserData> test) {
        List<ExoFixture> result = new ArrayList<>();
        float radiusSquared = radius * radius;
        for (ExoBody body : bodies) {
            if (body.dynamic && body.active) {
                result.addAll(body.circleTest(x, y, radius, radiusSquared, test));
            }
        }
        return result;
    }
}
