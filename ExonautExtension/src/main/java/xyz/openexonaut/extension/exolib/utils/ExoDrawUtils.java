package xyz.openexonaut.extension.exolib.utils;

import java.awt.*;

import xyz.openexonaut.extension.exolib.geo.*;

public final class ExoDrawUtils {
    private ExoDrawUtils() {}

    public static void fillCapsule(
            Graphics g,
            Color topColor,
            Color cylinderColor,
            Color bottomColor,
            float centerX,
            float centerY,
            float radius,
            float height,
            float scale) {
        ExoInt2DVector originTop =
                new Exo2DVector(centerX - radius, centerY + height / 2f + radius)
                        .convertNativeToDraw(scale);
        ExoInt2DVector originBottom =
                new Exo2DVector(centerX - radius, centerY - height / 2f + radius)
                        .convertNativeToDraw(scale);
        ExoInt2DVector originCylinder =
                new Exo2DVector(centerX - radius, centerY + height / 2f).convertNativeToDraw(scale);

        int scaledDoubleRadius = (int) (radius * 2f * scale);
        int scaledHeight = (int) (height * scale);

        g.setColor(topColor);
        g.fillOval(originTop.x, originTop.y, scaledDoubleRadius, scaledDoubleRadius);

        g.setColor(bottomColor);
        g.fillOval(originBottom.x, originBottom.y, scaledDoubleRadius, scaledDoubleRadius);

        g.setColor(cylinderColor);
        g.fillRect(originCylinder.x, originCylinder.y, scaledDoubleRadius, scaledHeight);
    }
}
