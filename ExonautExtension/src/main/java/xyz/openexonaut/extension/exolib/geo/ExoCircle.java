/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package xyz.openexonaut.extension.exolib.geo;

import xyz.openexonaut.extension.exolib.utils.*;

public class ExoCircle extends ExoShape {
    public final Exo2DVector center;
    public final float radius;
    public final float radiusSquared;

    public ExoCircle(Exo2DVector center, float radius) {
        this.center = center;
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }

    @Override
    public ExoShape offset(Exo2DVector position) {
        return new ExoCircle(center.plus(position.x, position.y), radius);
    }

    // SPDX-SnippetBegin
    /*
        SPDX-SnippetCopyrightText: 2009-2020 bobobobo et al, 2025-2026 OpenExonaut Contributors
        SPDX-License-Identifier: CC-BY-SA-4.0

        Source - https://stackoverflow.com/a/1084899
        Retrieved 2025-12-01
        Posted by bobobobo, modified by community. See post 'Timeline' for change history

        Modified to include full-insideness and for optimization and fitness for purpose.
    */
    @Override
    public ExoLineTestResult testLine(
            float startX, float startY, float dx, float dy, float length) {
        float fX = startX - center.x;
        float fY = startY - center.y;

        // start point in circle (original algorithm does not count fully-inside segments)
        if (Math.sqrt(fX * fX + fY * fY) <= radius)
            return new ExoLineTestResult(startX, startY, 0f);

        float a = ExoMathUtils.dot(dx, dy, dx, dy);
        float b = 2f * (ExoMathUtils.dot(fX, fY, dx, dy));
        float c = ExoMathUtils.dot(fX, fY, fX, fY) - radiusSquared;

        float discriminant = b * b - 4f * a * c;
        if (discriminant < 0f) return null;

        discriminant = (float) Math.sqrt(discriminant);
        a *= 2f;
        b = -b;

        // t1 is always closer than t2
        float t = (b - discriminant) / a;
        if (t < 0f || t > 1f) {
            t = (b + discriminant) / a;
            if (t < 0f || t > 1f) return null;
        }

        return new ExoLineTestResult(startX + t * dx, startY + t * dy, t * length);
    }

    // SPDX-SnippetEnd

    @Override
    public boolean testCircle(float x, float y, float radius, float radiusSquared) {
        float dx = center.x - x;
        float dy = center.y - y;
        float radialSum = this.radius + radius;
        return dx * dx + dy * dy <= radialSum * radialSum;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ExoCircle) {
            ExoCircle other = (ExoCircle) o;
            return center.equals(other.center) && radius == other.radius;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return center.hashCode() ^ Float.floatToIntBits(radius);
    }

    @Override
    public String toString() {
        return String.format("{%s, %f}", center, radius);
    }
}
