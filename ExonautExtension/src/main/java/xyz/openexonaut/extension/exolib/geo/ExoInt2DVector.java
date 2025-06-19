package xyz.openexonaut.extension.exolib.geo;

public class ExoInt2DVector {
    public final int x;
    public final int y;

    public ExoInt2DVector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public ExoInt2DVector scale(float scalar) {
        return new ExoInt2DVector((int) (x * scalar), (int) (y * scalar));
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }
}
