package xyz.openexonaut.extension.exolib;

import java.awt.*;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoMap {
    public final Image image;
    public final ExoInt2DVector translate;
    public final ExoInt2DVector size;

    public ExoMap (Image image, ExoInt2DVector translate, ExoInt2DVector size) {
        this.image = image;
        this.translate = translate;
        this.size = size;
    }

    public void draw (Graphics g) {
        g.drawImage(image, 0, 0, null);
    }
}
