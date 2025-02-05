package xyz.openexonaut.extension.exolib;

import java.awt.*;

import physx.geometry.*;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoMap {
    public final PxTriangleMeshGeometry triangleMesh;
    public final Image image;
    public final ExoInt2DVector translate;
    public final ExoInt2DVector size;
    public final float scale;

    public ExoMap(PxTriangleMeshGeometry triangleMesh, Image image, ExoInt2DVector translate, ExoInt2DVector size, float scale) {
        this.triangleMesh = triangleMesh;
        this.image = image;
        this.translate = translate;
        this.size = size;
        this.scale = scale;
    }
}
