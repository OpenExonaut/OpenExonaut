package xyz.openexonaut.extension.exolib.geo;

public class Exo2DVector {
    public final float x;
    public final float y;

    public Exo2DVector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public ExoInt2DVector convertNativeToDraw(float scalar) {
        return new ExoInt2DVector((int) (x * scalar), (int) (-y * scalar));
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
