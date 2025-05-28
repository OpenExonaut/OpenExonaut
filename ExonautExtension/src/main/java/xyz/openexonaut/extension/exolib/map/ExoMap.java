package xyz.openexonaut.extension.exolib.map;

import java.awt.*;

import com.badlogic.gdx.physics.box2d.*;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoMap {
    static {
        Box2D.init();
    }

    public final FixtureDef[] wallFixtureDefs;
    public final Exo2DVector[] teamPlayerSpawns;
    public final Exo2DVector[] ffaPlayerSpawns;
    public final ExoItemSpawner[] teamItemSpawns;
    public final ExoItemSpawner[] ffaItemSpawns;
    public final Image image;
    public final ExoInt2DVector translate;
    public final ExoInt2DVector size;
    public final float scale;

    private boolean finalizedItemSpawns = false;

    public ExoMap(
            FixtureDef[] wallFixtureDefs,
            Exo2DVector[] teamPlayerSpawns,
            Exo2DVector[] ffaPlayerSpawns,
            ExoItemSpawner[] teamItemSpawns,
            ExoItemSpawner[] ffaItemSpawns,
            Image image,
            ExoInt2DVector translate,
            ExoInt2DVector size,
            float scale) {
        this.wallFixtureDefs = wallFixtureDefs;
        this.teamPlayerSpawns = teamPlayerSpawns;
        this.ffaPlayerSpawns = ffaPlayerSpawns;
        this.teamItemSpawns = teamItemSpawns;
        this.ffaItemSpawns = ffaItemSpawns;
        this.image = image;
        this.translate = translate;
        this.size = size;
        this.scale = scale;
    }

    public void finishedFinalization() {
        finalizedItemSpawns = true;
    }

    public boolean finalized() {
        return finalizedItemSpawns;
    }

    public void destroy() {
        for (FixtureDef wallFixtureDef : wallFixtureDefs) {
            wallFixtureDef.shape.dispose();
        }
    }

    public void draw(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }
}
