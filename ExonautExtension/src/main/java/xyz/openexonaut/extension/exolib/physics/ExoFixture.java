package xyz.openexonaut.extension.exolib.physics;

import java.util.function.*;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoFixture {
    public final ExoShape shape;
    public final ExoUserData userData;

    public ExoShape offsetShape = null;

    // only to be used by ExoBody::createFixture
    public ExoFixture(ExoFixtureDef fixtureDef, ExoUserData userData) {
        this.shape = fixtureDef.shape;
        this.userData = userData;
    }

    public ExoRaycastResult rayCast(
            ExoRaycastResult current,
            Predicate<ExoUserData> test,
            float startX,
            float startY,
            float dx,
            float dy,
            float length,
            ExoShape offsetShape) {
        if (!test.test(userData)) return current;

        ExoLineTestResult result = offsetShape.testLine(startX, startY, dx, dy, length);
        if (result == null) return current;

        return result.dist < current.dist
                ? new ExoRaycastResult(result.x, result.y, this, result.dist)
                : current;
    }

    public boolean circleTest(
            float x,
            float y,
            float radius,
            float radiusSquared,
            Predicate<ExoUserData> test,
            ExoShape offsetShape) {
        if (!test.test(userData)) return false;
        return offsetShape.testCircle(x, y, radius, radiusSquared);
    }
}
