package xyz.openexonaut.extension.exolib.geo;

public class ExoLineSegment {
    public final Exo2DVector vertexOne;
    public final Exo2DVector vertexTwo;

    public ExoLineSegment(Exo2DVector vertexOne, Exo2DVector vertexTwo) {
        this.vertexOne = vertexOne;
        this.vertexTwo = vertexTwo;
    }

    public boolean isDegenerate() {
        return vertexOne.equals(vertexTwo);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ExoLineSegment) {
            ExoLineSegment other = (ExoLineSegment) o;
            return (vertexOne.equals(other.vertexOne) && vertexTwo.equals(other.vertexTwo))
                    || (vertexOne.equals(other.vertexTwo) && vertexTwo.equals(other.vertexOne));
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (isDegenerate()) return vertexOne.hashCode(); // avoid 0 being common
        return vertexOne.hashCode() ^ vertexTwo.hashCode();
    }

    @Override
    public String toString() {
        return "{" + vertexOne + ", " + vertexTwo + "}";
    }
}
