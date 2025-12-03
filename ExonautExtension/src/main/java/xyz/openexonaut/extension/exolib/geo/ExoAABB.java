package xyz.openexonaut.extension.exolib.geo;

public class ExoAABB extends ExoShape {
    public final Exo2DVector center;
    public final float halfWidth;
    public final float halfHeight;

    public final float width;
    public final float height;
    public final float minX;
    public final float minY;
    public final float maxX;
    public final float maxY;

    public ExoAABB(Exo2DVector center, float halfWidth, float halfHeight) {
        this.center = center;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;

        this.width = halfWidth * 2f;
        this.height = halfHeight * 2f;
        this.minX = center.x - halfWidth;
        this.minY = center.y - halfHeight;
        this.maxX = center.x + halfWidth;
        this.maxY = center.y + halfHeight;
    }

    @Override
    public ExoShape offset(Exo2DVector position) {
        return new ExoAABB(center.plus(position.x, position.y), halfWidth, halfHeight);
    }

    // https://noonat.github.io/intersect/#aabb-vs-segment
    @Override
    public ExoLineTestResult testLine(
            float startX, float startY, float dx, float dy, float length) {
        // start point in AABB (original algorithm does not count fully-inside line segments)
        if (startX >= minX && startX <= maxX && startY >= minY && startY <= maxY) {
            return new ExoLineTestResult(startX, startY, 0f);
        }

        float scaleX = 1f / dx;
        float scaleY = 1f / dy;
        float signX = Math.signum(scaleX);
        float signY = Math.signum(scaleY);

        float nearTimeX = (center.x - signX * halfWidth - startX) * scaleX;
        float farTimeY = (center.y + signY * halfHeight - startY) * scaleY;
        if (nearTimeX > farTimeY) return null;

        float nearTimeY = (center.y - signY * halfHeight - startY) * scaleY;
        float farTimeX = (center.x + signX * halfWidth - startX) * scaleX;
        if (nearTimeY > farTimeX) return null;

        float nearTime = Math.max(nearTimeX, nearTimeY);
        if (nearTime > 1f || Math.min(farTimeX, farTimeY) < 0f) return null;

        float t = Math.max(0f, nearTime); // already checked nearTime not greater than 1
        return new ExoLineTestResult(startX + t * dx, startY + t * dy, t * length);
    }

    // https://gamedev.stackexchange.com/a/178154
    @Override
    public boolean testCircle(float x, float y, float radius, float radiusSquared) {
        float distanceX = x - center.x;
        float distanceY = y - center.y;

        float clampDstX = Math.max(minX, Math.min(distanceX, maxX));
        float clampDstY = Math.max(minY, Math.min(distanceY, maxY));

        float closestPointX = center.x + clampDstX;
        float closestPointY = center.y + clampDstY;

        float distX = closestPointX - x;
        float distY = closestPointY - y;

        return distX * distX + distY * distY <= radiusSquared;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ExoAABB) {
            ExoAABB other = (ExoAABB) o;
            return center.equals(other.center)
                    && halfWidth == other.halfWidth
                    && halfHeight == other.halfHeight;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return center.hashCode()
                ^ Float.floatToIntBits(halfWidth)
                ^ Float.floatToIntBits(halfHeight);
    }

    @Override
    public String toString() {
        return String.format("{%s, %f, %f}", center, halfWidth, halfHeight);
    }
}
