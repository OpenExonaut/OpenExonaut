package xyz.openexonaut.extension.exolib.geo;

public class Exo2DVector {
    public static final Exo2DVector ZERO = new Exo2DVector(0f, 0f);

    public final float x;
    public final float y;

    public Exo2DVector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Exo2DVector plus(float x, float y) {
        return new Exo2DVector(this.x + x, this.y + y);
    }

    public Exo2DVector withMagnitude(float length) {
        double factor = length / Math.sqrt(x * x + y * y);
        return new Exo2DVector((float) (x * factor), (float) (y * factor));
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
        return String.format("(%f, %f)", x, y);
    }
}
