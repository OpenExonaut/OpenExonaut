package xyz.openexonaut.extension.exolib.resources;

import java.nio.file.*;

import xyz.openexonaut.extension.exolib.geo.*;
import xyz.openexonaut.extension.exolib.map.*;

public class ExoMapManager {
    private final int mapCount;
    private final ExoMap[] maps;

    public ExoMapManager(Path worldsFolder, int mapCount, float debugGFXScale) {
        this.mapCount = mapCount;
        maps = new ExoMap[mapCount];

        for (int i = 0; i < mapCount; i++) {
            // world 0 exists, but is the tutorial world, and the tutorial is always run locally
            ExoMapLoader mapLoader =
                    new ExoMapLoader(worldsFolder.resolve(String.format("world_%d", i + 1)));

            if (debugGFXScale != 0f) {
                ExoInt2DVector scaledDrawTranslate = mapLoader.getDrawTranslate(debugGFXScale);
                ExoInt2DVector scaledDrawSize = mapLoader.getDrawSize(debugGFXScale);
                maps[i] =
                        new ExoMap(
                                mapLoader.wallFixtureDefs,
                                mapLoader.teamPlayerSpawns,
                                mapLoader.ffaPlayerSpawns,
                                mapLoader.teamItemSpawns,
                                mapLoader.ffaItemSpawns,
                                mapLoader.getImage(
                                        debugGFXScale, scaledDrawTranslate, scaledDrawSize),
                                scaledDrawTranslate,
                                scaledDrawSize,
                                debugGFXScale);
            } else {
                maps[i] =
                        new ExoMap(
                                mapLoader.wallFixtureDefs,
                                mapLoader.teamPlayerSpawns,
                                mapLoader.ffaPlayerSpawns,
                                mapLoader.teamItemSpawns,
                                mapLoader.ffaItemSpawns,
                                null,
                                null,
                                null,
                                0f);
            }
        }
    }

    public int getMapCount() {
        return mapCount;
    }

    public ExoMap getMap(int mapId) {
        return maps[mapId - 1];
    }

    public void destroy() {
        for (ExoMap map : maps) {
            if (map != null) {
                map.destroy();
            }
        }
    }
}
