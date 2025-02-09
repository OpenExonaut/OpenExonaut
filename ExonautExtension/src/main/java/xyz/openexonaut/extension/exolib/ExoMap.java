package xyz.openexonaut.extension.exolib;

import java.awt.*;

import com.badlogic.gdx.physics.box2d.*;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoMap {
    static {
        Box2D.init();
    }

    public final FixtureDef[] wallFixtures;
    public final Image image;
    public final ExoInt2DVector translate;
    public final ExoInt2DVector size;
    public final float scale;

    public ExoMap(
            FixtureDef[] wallFixtures,
            Image image,
            ExoInt2DVector translate,
            ExoInt2DVector size,
            float scale) {
        this.wallFixtures = wallFixtures;
        this.image = image;
        this.translate = translate;
        this.size = size;
        this.scale = scale;
    }

    public void draw(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }
}
