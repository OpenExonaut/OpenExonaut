package xyz.openexonaut.extension.exolib.utils;

public final class ExoMathUtils {
    private ExoMathUtils() {}

    public static float dot(float x1, float y1, float x2, float y2) {
        return x1 * x2 + y1 * y2;
    }
}
