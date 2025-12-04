package xyz.openexonaut.extension.exolib.geo;

import xyz.openexonaut.extension.exolib.utils.*;

public class ExoLineSegment extends ExoShape {
    public final Exo2DVector vertexOne;
    public final Exo2DVector vertexTwo;

    public final float lenX;
    public final float lenY;
    public final float length;
    public final float doubleLenDotProduct;
    public final float quadrupleLenDotProduct;

    public ExoLineSegment(Exo2DVector vertexOne, Exo2DVector vertexTwo) {
        this.vertexOne = vertexOne;
        this.vertexTwo = vertexTwo;

        lenX = vertexTwo.x - vertexOne.x;
        lenY = vertexTwo.y - vertexOne.y;
        length = (float) Math.sqrt(lenX * lenX + lenY * lenY);
        doubleLenDotProduct = 2f * ExoMathUtils.dot(lenX, lenY, lenX, lenY);
        quadrupleLenDotProduct = 2f * doubleLenDotProduct;
    }

    @Override
    public ExoShape offset(Exo2DVector position) {
        return new ExoLineSegment(
                vertexOne.plus(position.x, position.y), vertexTwo.plus(position.x, position.y));
    }

    // https://stackoverflow.com/a/3746601
    @Override
    public ExoLineTestResult testLine(
            float startX, float startY, float dx, float dy, float length) {
        float bDotDPerp = dx * lenY - dy * lenX;

        // parallel
        if (bDotDPerp == 0f) return null;

        float cx = vertexOne.x - startX;
        float cy = vertexOne.y - startY;

        float t = (cx * lenY - cy * lenX) / bDotDPerp;
        if (t < 0f || t > 1f) return null;

        float u = (cx * dy - cy * dx) / bDotDPerp;
        if (u < 0f || u > 1f) return null;

        return new ExoLineTestResult(startX + t * dx, startY + t * dy, t * length);
    }

    // https://stackoverflow.com/a/1084899
    @Override
    public boolean testCircle(float x, float y, float radius, float radiusSquared) {
        float fX = vertexOne.x - x;
        float fY = vertexOne.y - y;

        // start point in circle (original algorithm does not count fully-inside segments)
        if (fX * fX + fY * fY <= radiusSquared) return true;

        float b = 2f * (ExoMathUtils.dot(fX, fY, lenX, lenY));
        float c = ExoMathUtils.dot(fX, fY, fX, fY) - radiusSquared;

        float discriminant = b * b - quadrupleLenDotProduct * c;
        if (discriminant < 0f) return false;

        discriminant = (float) Math.sqrt(discriminant);
        b = -b;

        float t1 = (b - discriminant) / doubleLenDotProduct;
        if (t1 >= 0f && t1 <= 1f) return true;
        float t2 = (b + discriminant) / doubleLenDotProduct;
        return t2 >= 0f && t2 <= 1f;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ExoLineSegment) {
            ExoLineSegment other = (ExoLineSegment) o;
            return (vertexOne.equals(other.vertexOne) && vertexTwo.equals(other.vertexTwo))
                    || (vertexOne.equals(other.vertexTwo) && vertexTwo.equals(other.vertexOne));
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (vertexOne.equals(vertexTwo)) return vertexOne.hashCode(); // avoid 0 being common
        return vertexOne.hashCode() ^ vertexTwo.hashCode();
    }

    @Override
    public String toString() {
        return String.format("{%s, %s}", vertexOne, vertexTwo);
    }
}
