package xyz.openexonaut.extension.exolib.geo;

public class Exo3DVector {
    public final float x;
    public final float y;
    public final float z;

    public Exo3DVector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Exo3DVector getTransformed(
            String selfRotation,
            Exo3DVector selfScale,
            Exo3DVector selfPosition,
            String fatherRotation,
            Exo3DVector fatherScale,
            Exo3DVector fatherPosition) {
        return applyScaleRotatePosition(selfScale, selfPosition, selfRotation)
                .applyScaleRotatePosition(fatherScale, fatherPosition, fatherRotation);
    }

    private Exo3DVector applyScaleRotatePosition(
            Exo3DVector scale, Exo3DVector position, String rotation) {
        float newX = 0f;
        float newY = 0f;
        float newZ = 0f;
        switch (rotation) {
            case "0":
                newX = x * scale.x;
                newY = y * scale.y;
                newZ = z * scale.z;
                break;
            case "-x":
                newX = x * scale.x;
                newY = z * scale.z;
                newZ = -y * scale.y;
                break;
            case "z":
                newX = y * scale.y;
                newY = -x * scale.x;
                newZ = z * scale.z;
                break;
            default:
                System.err.println("Unknown rotation " + rotation + "!");
                System.exit(1);
        }
        // don't know why it's - for x position and + for y/z position
        return new Exo3DVector(newX - position.x, newY + position.y, newZ + position.z);
    }

    public ExoInt2DVector convertNativeToDraw(float scalar) {
        return new ExoInt2DVector((int) (x * scalar), (int) (-y * scalar));
    }

    public Exo2DVector discardZ() {
        return new Exo2DVector(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
