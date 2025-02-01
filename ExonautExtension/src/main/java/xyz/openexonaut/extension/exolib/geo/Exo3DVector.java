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
            ExoRotation selfRotation,
            Exo3DVector selfScale,
            Exo3DVector selfPosition,
            ExoRotation fatherRotation,
            Exo3DVector fatherScale,
            Exo3DVector fatherPosition) {
        return this.applyScaleRotatePosition(selfScale, selfPosition, selfRotation)
                .applyScaleRotatePosition(fatherScale, fatherPosition, fatherRotation);
    }

    private Exo3DVector applyScaleRotatePosition(
            Exo3DVector scale, Exo3DVector position, ExoRotation rotation) {
        float newX = 0;
        float newY = 0;
        float newZ = 0;
        switch (rotation) {
            case NONE:
                newX = this.x * scale.x;
                newY = this.y * scale.y;
                newZ = this.z * scale.z;
                break;
            case MINUS_X:
                newX = this.x * scale.x;
                newY = this.z * scale.z;
                newZ = -this.y * scale.y;
                break;
            case Z:
                newX = this.y * scale.y;
                newY = -this.x * scale.x;
                newZ = this.z * scale.z;
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

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
