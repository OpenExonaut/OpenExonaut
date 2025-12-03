package xyz.openexonaut.extension.exolib.geo;

public abstract class ExoShape {
    public abstract ExoShape offset(Exo2DVector position);

    public abstract ExoLineTestResult testLine(
            float startX, float startY, float dx, float dy, float length);

    public abstract boolean testCircle(float x, float y, float radius, float radiusSquared);
}
