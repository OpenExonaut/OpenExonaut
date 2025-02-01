package xyz.openexonaut.extension.exolib.geo;

public enum ExoRotation {
    UNKNOWN("?"),
    NONE("0"),
    MINUS_X("-x"),
    Z("z");

    public final String descriptor;
    private ExoRotation (String descriptor) {
        this.descriptor = descriptor;
    }

    public static ExoRotation getRotation (String descriptor) {
        for (ExoRotation e : ExoRotation.values()) {
            if (descriptor.equals(e.descriptor)) return e;
        }
        System.err.println("Unknown rotation descriptor " + descriptor);
        return UNKNOWN;
    }
}
