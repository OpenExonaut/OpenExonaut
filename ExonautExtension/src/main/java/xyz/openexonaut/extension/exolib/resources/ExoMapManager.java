package xyz.openexonaut.extension.exolib.resources;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.geo.*;
import xyz.openexonaut.extension.exolib.map.*;
import xyz.openexonaut.extension.exolib.physics.*;

public final class ExoMapManager {
    private static final String comma = Pattern.quote(",");
    private static final String commaSpace = Pattern.quote(", ");
    private static final String colonSpace = Pattern.quote(": ");
    private static final String newline = Pattern.quote("\n");

    private static ExoMap[] maps = new ExoMap[0];

    private ExoMapManager() {}

    public static void init(Path worldsFolder, int mapCount, float debugGFXScale) {
        maps = new ExoMap[mapCount];

        // world_0 exists, but is the tutorial world, and the tutorial is always run locally
        for (int i = 1; i <= mapCount; i++) {
            maps[i - 1] = load(worldsFolder.resolve(String.format("world_%d", i)), debugGFXScale);
        }
    }

    public static int getMapCount() {
        return maps.length;
    }

    public static ExoMap getMap(int mapId) {
        return maps[mapId - 1];
    }

    private static ExoMap load(Path path, float debugGFXScale) {
        Path spawnsFolder = path.resolve("spawns");
        Path pickupsFolder = path.resolve("pickups");

        Map<String, String> info = loadInfo(path.resolve("collision_info.txt"));

        ExoLineSegment[] segments =
                readSegments(
                        path.resolve("collision_vertices.txt"),
                        path.resolve("collision_polygons.txt"),
                        info.get("rotation"),
                        get3DVector(info.get("scale")),
                        get3DVector(info.get("position")),
                        info.get("father_rotation"),
                        get3DVector(info.get("father_scale")),
                        get3DVector(info.get("father_position")));

        ExoFixtureDef[] wallFixtureDefs = new ExoFixtureDef[segments.length];
        for (int i = 0; i < wallFixtureDefs.length; i++) {
            wallFixtureDefs[i] = new ExoFixtureDef(segments[i]);
        }

        BufferedImage image = null;
        ExoInt2DVector scaledDrawTranslate = null;
        ExoInt2DVector scaledDrawSize = null;

        if (debugGFXScale != 0f) {
            scaledDrawTranslate = getInt2DVector(info.get("draw_translate")).scale(debugGFXScale);
            scaledDrawSize = getInt2DVector(info.get("draw_size")).scale(debugGFXScale);

            image =
                    new BufferedImage(
                            scaledDrawSize.x, scaledDrawSize.y, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, scaledDrawSize.x, scaledDrawSize.y);

            g2d.translate(scaledDrawTranslate.x, scaledDrawTranslate.y);

            g2d.setColor(Color.BLACK);
            for (ExoLineSegment segment : segments) {
                ExoInt2DVector pointOne = segment.vertexOne.convertNativeToDraw(debugGFXScale);
                ExoInt2DVector pointTwo = segment.vertexTwo.convertNativeToDraw(debugGFXScale);

                g2d.drawLine(pointOne.x, pointOne.y, pointTwo.x, pointTwo.y);
            }

            g2d.translate(-scaledDrawTranslate.x, -scaledDrawTranslate.y);
        }

        return new ExoMap(
                wallFixtureDefs,
                loadPlayerSpawns(spawnsFolder.resolve("t.txt")),
                loadPlayerSpawns(spawnsFolder.resolve("b.txt")),
                loadItemSpawns(pickupsFolder.resolve("t.txt")),
                loadItemSpawns(pickupsFolder.resolve("b.txt")),
                image,
                scaledDrawTranslate,
                scaledDrawSize,
                debugGFXScale);
    }

    private static ExoInt2DVector getInt2DVector(String property) {
        String[] pairStrings = property.split(commaSpace);
        return new ExoInt2DVector(
                Integer.parseInt(pairStrings[0]), Integer.parseInt(pairStrings[1]));
    }

    private static Exo3DVector get3DVector(String property) {
        String[] tripletStrings = property.split(commaSpace);
        return new Exo3DVector(
                Float.parseFloat(tripletStrings[0]),
                Float.parseFloat(tripletStrings[1]),
                Float.parseFloat(tripletStrings[2]));
    }

    private static Map<String, String> loadInfo(Path path) {
        String[] infoStrings;
        try {
            infoStrings =
                    Files.readString(path)
                            .strip()
                            .replace("\r", "")
                            .replace("\n\n", "\n")
                            .split(newline);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> info = new HashMap<>();
        for (String s : infoStrings) {
            String[] pair = s.split(colonSpace, 2);
            info.put(pair[0], pair[1]);
        }

        return info;
    }

    private static ExoLineSegment[] readSegments(
            Path coordFile,
            Path triangleFile,
            String selfRotation,
            Exo3DVector selfScale,
            Exo3DVector selfPosition,
            String fatherRotation,
            Exo3DVector fatherScale,
            Exo3DVector fatherPosition) {
        String[] coordStrings = readFbxExtract(coordFile);
        String[] vertexStrings = readFbxExtract(triangleFile);

        Exo2DVector[] coords = new Exo2DVector[coordStrings.length / 3];
        for (int i = 0; i < coords.length; i++) {
            coords[i] =
                    new Exo3DVector(
                                    Float.parseFloat(coordStrings[i * 3]),
                                    Float.parseFloat(coordStrings[(i * 3) + 1]),
                                    Float.parseFloat(coordStrings[(i * 3) + 2]))
                            .getTransformed(
                                    selfRotation,
                                    selfScale,
                                    selfPosition,
                                    fatherRotation,
                                    fatherScale,
                                    fatherPosition)
                            .discardZ();
        }

        List<ExoLineSegment> segments = new ArrayList<>();
        for (int i = 0; i < vertexStrings.length / 3; i++) {
            Exo2DVector v1 = coords[Integer.parseInt(vertexStrings[i * 3])];
            Exo2DVector v2 = coords[Integer.parseInt(vertexStrings[(i * 3) + 1])];
            Exo2DVector v3 = coords[-(Integer.parseInt(vertexStrings[(i * 3) + 2])) - 1];

            addWithSieve(v1, v2, segments);
            addWithSieve(v1, v3, segments);
            addWithSieve(v2, v3, segments);
        }

        return segments.toArray(new ExoLineSegment[0]);
    }

    private static Exo2DVector[] loadPlayerSpawns(Path path) {
        String[] playerSpawnStrings = readTextAsset(path);
        Exo2DVector[] playerSpawns = new Exo2DVector[playerSpawnStrings.length / 2];
        for (int i = 0; i < playerSpawns.length; i++) {
            playerSpawns[i] =
                    new Exo2DVector(
                            Float.parseFloat(playerSpawnStrings[i * 2]),
                            Float.parseFloat(playerSpawnStrings[(i * 2) + 1]));
        }

        return playerSpawns;
    }

    private static ExoItemSpawner[] loadItemSpawns(Path path) {
        String[] itemSpawnStrings = readTextAsset(path);
        ExoItemSpawner[] itemSpawns = new ExoItemSpawner[itemSpawnStrings.length / 4];
        for (int i = 0; i < itemSpawns.length; i++) {
            itemSpawns[i] =
                    new ExoItemSpawner(
                            ExoPickupEnum.get(Integer.parseInt(itemSpawnStrings[i * 4])),
                                    Float.parseFloat(itemSpawnStrings[(i * 4) + 1]),
                            Float.parseFloat(itemSpawnStrings[(i * 4) + 2]),
                                    Float.parseFloat(itemSpawnStrings[(i * 4) + 3]));
        }

        return itemSpawns;
    }

    private static String[] readFbxExtract(Path path) {
        try {
            return Files.readString(path).replace("\r", "").replace("\n", "").split(comma);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] readTextAsset(Path path) {
        try {
            return Files.readString(path).replace("\r", "").replace("\n", ", ").split(commaSpace);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addWithSieve(
            Exo2DVector vertexOne, Exo2DVector vertexTwo, List<ExoLineSegment> segments) {
        if (!vertexOne.equals(vertexTwo)) {
            ExoLineSegment segment = new ExoLineSegment(vertexOne, vertexTwo);
            for (int i = 0; i < segments.size(); i++) {
                if (segments.get(i).equals(segment)) return;
            }
            segments.add(segment);
        }
    }

    private static class Exo3DVector {
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
                    throw new RuntimeException(String.format("Unknown rotation %s!", rotation));
            }
            // don't know why it's - for x position and + for y/z position
            return new Exo3DVector(newX - position.x, newY + position.y, newZ + position.z);
        }

        public Exo2DVector discardZ() {
            return new Exo2DVector(x, y);
        }

        @Override
        public String toString() {
            return String.format("(%f, %f, %f)", x, y, z);
        }
    }
}
