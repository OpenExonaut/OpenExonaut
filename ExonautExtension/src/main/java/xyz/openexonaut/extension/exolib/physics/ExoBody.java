package xyz.openexonaut.extension.exolib.physics;

import java.util.*;
import java.util.function.*;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoBody {
    private final List<ExoFixture> fixtureList = new ArrayList<>();
    public final boolean dynamic;

    public boolean active = true;
    private Exo2DVector position = Exo2DVector.ZERO;

    // only to be used by ExoSim::createBody
    public ExoBody(ExoBodyDef bodyDef) {
        this.dynamic = bodyDef.dynamic;
        this.position = bodyDef.position;
    }

    public Exo2DVector getPosition() {
        return position;
    }

    public void setPosition(Exo2DVector position) {
        this.position = position;
        for (ExoFixture fixture : fixtureList) {
            fixture.offsetShape = null;
        }
    }

    public ExoFixture createFixture(ExoFixtureDef fixtureDef, ExoUserData userData) {
        ExoFixture retVal = new ExoFixture(fixtureDef, userData);
        fixtureList.add(retVal);
        return retVal;
    }

    public List<ExoFixture> getFixtureList() {
        return fixtureList;
    }

    private ExoShape offset(ExoFixture fixture) {
        if (position == Exo2DVector.ZERO || position.equals(Exo2DVector.ZERO)) {
            return fixture.shape;
        }
        if (fixture.offsetShape != null) {
            return fixture.offsetShape;
        }
        return fixture.offsetShape = fixture.shape.offset(position);
    }

    public ExoRaycastResult rayCast(
            ExoRaycastResult current,
            Predicate<ExoUserData> test,
            float startX,
            float startY,
            float dx,
            float dy,
            float length) {
        ExoRaycastResult result = current;
        for (ExoFixture fixture : fixtureList) {
            result = fixture.rayCast(result, test, startX, startY, dx, dy, length, offset(fixture));
            if (result.dist == 0f) break;
        }
        return result;
    }

    public List<ExoFixture> circleTest(
            float x, float y, float radius, float radiusSquared, Predicate<ExoUserData> test) {
        List<ExoFixture> result = new ArrayList<>(3);
        for (ExoFixture fixture : fixtureList) {
            if (fixture.circleTest(x, y, radius, radiusSquared, test, offset(fixture))) {
                result.add(fixture);
            }
        }
        return result;
    }
}
