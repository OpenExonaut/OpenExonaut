package xyz.openexonaut.extension.exolib.geo;

public class ExoTriangleVertices {
    public final int vertexOne;
    public final int vertexTwo;
    public final int vertexThree;

    public ExoTriangleVertices(int vertexOne, int vertexTwo, int vertexThree) {
        this.vertexOne = vertexOne;
        this.vertexTwo = vertexTwo;
        this.vertexThree = (-vertexThree) - 1;
    }

    @Override
    public String toString() {
        return "[" + vertexOne + ", " + vertexTwo + ", " + vertexThree + "]";
    }
}
