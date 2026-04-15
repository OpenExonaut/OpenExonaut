/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package xyz.openexonaut.extension.exolib.resources;

import java.awt.*;
import java.awt.image.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.geo.*;
import xyz.openexonaut.extension.exolib.map.*;
import xyz.openexonaut.extension.exolib.physics.*;

public final class ExoMapManager {
    private static ExoMap[] maps = new ExoMap[0];

    private ExoMapManager() {}

    public static void init(Path worldsFolder, ArrayNode spawnPickups, float debugGFXScale) {
        // world_0 exists, but is the tutorial world, and the tutorial is always run locally
        maps = new ExoMap[spawnPickups.size() - 1];

        for (int i = 1; i < spawnPickups.size(); i++) {
            try {
                maps[i - 1] =
                        load(
                                new ObjectMapper()
                                        .readTree(
                                                worldsFolder
                                                        .resolve(String.format("world_%d.json", i))
                                                        .toFile()),
                                spawnPickups.get(i),
                                debugGFXScale);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static int getMapCount() {
        return maps.length;
    }

    public static ExoMap getMap(int mapId) {
        return maps[mapId - 1];
    }

    private static ExoMap load(JsonNode collision, JsonNode spawnPickup, float debugGFXScale) {
        ExoLineSegment[] segments =
                readSegments(
                        (ArrayNode) collision.get("vertices"),
                        (ArrayNode) collision.get("polygons"),
                        collision.get("rotation").asText(),
                        get3DVector(collision.get("scale")),
                        get3DVector(collision.get("position")),
                        collision.get("father_rotation").asText(),
                        get3DVector(collision.get("father_scale")),
                        get3DVector(collision.get("father_position")));

        ExoFixtureDef[] wallFixtureDefs = new ExoFixtureDef[segments.length];
        for (int i = 0; i < wallFixtureDefs.length; i++) {
            wallFixtureDefs[i] = new ExoFixtureDef(segments[i]);
        }

        BufferedImage image = null;
        ExoInt2DVector scaledDrawTranslate = null;
        ExoInt2DVector scaledDrawSize = null;

        if (debugGFXScale != 0f) {
            scaledDrawTranslate =
                    getInt2DVector(collision.get("draw_translate")).scale(debugGFXScale);
            scaledDrawSize = getInt2DVector(collision.get("draw_size")).scale(debugGFXScale);

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
                loadPlayerSpawns((ArrayNode) spawnPickup.get("team").get("spawn")),
                loadPlayerSpawns((ArrayNode) spawnPickup.get("solo").get("spawn")),
                loadItemSpawns((ArrayNode) spawnPickup.get("team").get("pickup")),
                loadItemSpawns((ArrayNode) spawnPickup.get("solo").get("pickup")),
                image,
                scaledDrawTranslate,
                scaledDrawSize,
                debugGFXScale);
    }

    private static ExoInt2DVector getInt2DVector(JsonNode node) {
        return new ExoInt2DVector(
                Integer.parseInt(node.get("x").asText()), Integer.parseInt(node.get("y").asText()));
    }

    private static Exo3DVector get3DVector(JsonNode node) {
        return new Exo3DVector(
                Float.parseFloat(node.get("x").asText()),
                Float.parseFloat(node.get("y").asText()),
                Float.parseFloat(node.get("z").asText()));
    }

    private static ExoLineSegment[] readSegments(
            ArrayNode coordsNode,
            ArrayNode polygonsNode,
            String selfRotation,
            Exo3DVector selfScale,
            Exo3DVector selfPosition,
            String fatherRotation,
            Exo3DVector fatherScale,
            Exo3DVector fatherPosition) {
        Exo2DVector[] coords = new Exo2DVector[coordsNode.size()];
        for (int i = 0; i < coords.length; i++) {
            JsonNode coordSet = coordsNode.get(i);
            coords[i] =
                    new Exo3DVector(
                                    Float.parseFloat(coordSet.get("x").asText()),
                                    Float.parseFloat(coordSet.get("y").asText()),
                                    Float.parseFloat(coordSet.get("z").asText()))
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
        for (int i = 0; i < polygonsNode.size(); i++) {
            List<Exo2DVector> coordList = new ArrayList<>();

            ArrayNode coordSet = (ArrayNode) polygonsNode.get(i);
            for (int j = 0; j < coordSet.size(); j++) {
                coordList.add(coords[Integer.parseInt(coordSet.get(j).asText())]);
            }

            addWithSieve(coordList.get(0), coordList.get(coordList.size() - 1), segments);
            for (int j = 1; j < coordList.size(); j++) {
                addWithSieve(coordList.get(j - 1), coordList.get(j), segments);
            }
        }

        return segments.toArray(new ExoLineSegment[0]);
    }

    private static Exo2DVector[] loadPlayerSpawns(ArrayNode nodes) {
        Exo2DVector[] playerSpawns = new Exo2DVector[nodes.size()];
        for (int i = 0; i < playerSpawns.length; i++) {
            JsonNode node = nodes.get(i);
            playerSpawns[i] =
                    new Exo2DVector(
                            Float.parseFloat(node.get("x").asText()),
                            Float.parseFloat(node.get("y").asText()));
        }

        return playerSpawns;
    }

    private static ExoItemSpawner[] loadItemSpawns(ArrayNode nodes) {
        ExoItemSpawner[] itemSpawns = new ExoItemSpawner[nodes.size()];
        for (int i = 0; i < itemSpawns.length; i++) {
            JsonNode node = nodes.get(i);
            itemSpawns[i] =
                    new ExoItemSpawner(
                            ExoPickupEnum.get(Integer.parseInt(node.get("type").asText())),
                                    Float.parseFloat(node.get("respawn").asText()),
                            Float.parseFloat(node.get("x").asText()),
                                    Float.parseFloat(node.get("y").asText()));
        }

        return itemSpawns;
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
