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
    public boolean equals(Object o) {
        if (o instanceof Exo2DVector) {
            Exo2DVector other = (Exo2DVector) o;
            return x == other.x && y == other.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(x) ^ Integer.rotateRight(Float.floatToIntBits(y), 16);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
