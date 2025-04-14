package xyz.openexonaut.extension.zone;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.exolib.geo.*;

public class MapLoader {
    private final Exo3DVector[] modifiedCoords;
    private final ExoTriangleVertices[] triangles;
    private final ExoLineSegment[] lineSegments;
    private final Exo2DVector[] teamPlayerSpawns;
    private final Exo2DVector[] ffaPlayerSpawns;
    private final ExoItemSpawner[] teamItemSpawns;
    private final ExoItemSpawner[] ffaItemSpawns;
    private final ExoInt2DVector drawTranslate;
    private final ExoInt2DVector drawSize;

    private final String selfRotation;
    private final String fatherRotation;
    private final Exo3DVector selfScale;
    private final Exo3DVector selfPosition;
    private final Exo3DVector fatherScale;
    private final Exo3DVector fatherPosition;

    public MapLoader(String path) {
        String vertexFile = "";
        String polygonFile = "";
        String collisionInfoFile = "";

        String teamPlayerSpawnFile = "";
        String ffaPlayerSpawnFile = "";
        String teamItemSpawnFile = "";
        String ffaItemSpawnFile = "";
        try {
            vertexFile =
                    Files.readString(Paths.get(path + "/collision_vertices.txt"))
                            .replace("\r", "")
                            .replace("\n", "");
            polygonFile =
                    Files.readString(Paths.get(path + "/collision_polygons.txt"))
                            .replace("\r", "")
                            .replace("\n", "");
            collisionInfoFile =
                    Files.readString(Paths.get(path + "/collision_info.txt"))
                            .strip()
                            .replace("\r", "")
                            .replace("\n\n", "\n");

            teamPlayerSpawnFile =
                    Files.readString(Paths.get(path + "/spawns/t.txt"))
                            .replace("\r", "")
                            .replace("\n", ", ");
            ffaPlayerSpawnFile =
                    Files.readString(Paths.get(path + "/spawns/b.txt"))
                            .replace("\r", "")
                            .replace("\n", ", ");
            teamItemSpawnFile =
                    Files.readString(Paths.get(path + "/pickups/t.txt"))
                            .replace("\r", "")
                            .replace("\n", ", ");
            ffaItemSpawnFile =
                    Files.readString(Paths.get(path + "/pickups/b.txt"))
                            .replace("\r", "")
                            .replace("\n", ", ");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] vertexStrings = vertexFile.split(",");
        String[] polygonStrings = polygonFile.split(",");
        String[] collisionInfoStrings = collisionInfoFile.split("\n");
        String[] teamPlayerSpawnStrings = teamPlayerSpawnFile.split(", ");
        String[] ffaPlayerSpawnStrings = ffaPlayerSpawnFile.split(", ");
        String[] teamItemSpawnStrings = teamItemSpawnFile.split(", ");
        String[] ffaItemSpawnStrings = ffaItemSpawnFile.split(", ");

        selfRotation = getProperty(collisionInfoStrings, "rotation");
        fatherRotation = getProperty(collisionInfoStrings, "father_rotation");
        drawTranslate =
                getInt2DVector(getProperty(collisionInfoStrings, "draw_translate").split(", "));
        drawSize = getInt2DVector(getProperty(collisionInfoStrings, "draw_size").split(", "));
        selfScale = get3DVector(getProperty(collisionInfoStrings, "scale").split(", "));
        selfPosition = get3DVector(getProperty(collisionInfoStrings, "position").split(", "));
        fatherScale = get3DVector(getProperty(collisionInfoStrings, "father_scale").split(", "));
        fatherPosition =
                get3DVector(getProperty(collisionInfoStrings, "father_position").split(", "));

        modifiedCoords = new Exo3DVector[vertexStrings.length / 3];
        triangles = new ExoTriangleVertices[polygonStrings.length / 3];
        teamPlayerSpawns = new Exo2DVector[teamPlayerSpawnStrings.length / 2];
        ffaPlayerSpawns = new Exo2DVector[ffaPlayerSpawnStrings.length / 2];
        teamItemSpawns = new ExoItemSpawner[teamItemSpawnStrings.length / 4];
        ffaItemSpawns = new ExoItemSpawner[ffaItemSpawnStrings.length / 4];

        for (int i = 0; i < modifiedCoords.length; i++) {
            modifiedCoords[i] =
                    new Exo3DVector(
                                    Float.parseFloat(vertexStrings[i * 3]),
                                    Float.parseFloat(vertexStrings[(i * 3) + 1]),
                                    Float.parseFloat(vertexStrings[(i * 3) + 2]))
                            .getTransformed(
                                    selfRotation,
                                    selfScale,
                                    selfPosition,
                                    fatherRotation,
                                    fatherScale,
                                    fatherPosition);
        }

        for (int i = 0; i < triangles.length; i++) {
            triangles[i] =
                    new ExoTriangleVertices(
                            Integer.parseInt(polygonStrings[i * 3]),
                            Integer.parseInt(polygonStrings[(i * 3) + 1]),
                            Integer.parseInt(polygonStrings[(i * 3) + 2]));
        }

        for (int i = 0; i < teamPlayerSpawns.length; i++) {
            teamPlayerSpawns[i] =
                    new Exo2DVector(
                            Float.parseFloat(teamPlayerSpawnStrings[i * 2]),
                            Float.parseFloat(teamPlayerSpawnStrings[(i * 2) + 1]));
        }
        for (int i = 0; i < ffaPlayerSpawns.length; i++) {
            ffaPlayerSpawns[i] =
                    new Exo2DVector(
                            Float.parseFloat(ffaPlayerSpawnStrings[i * 2]),
                            Float.parseFloat(ffaPlayerSpawnStrings[(i * 2) + 1]));
        }

        for (int i = 0; i < teamItemSpawns.length; i++) {
            teamItemSpawns[i] =
                    new ExoItemSpawner(
                            ExoPickupEnum.get(Integer.parseInt(teamItemSpawnStrings[i * 4])),
                                    Float.parseFloat(teamItemSpawnStrings[(i * 4) + 1]),
                            Float.parseFloat(teamItemSpawnStrings[(i * 4) + 2]),
                                    Float.parseFloat(teamItemSpawnStrings[(i * 4) + 3]));
        }
        for (int i = 0; i < ffaItemSpawns.length; i++) {
            ffaItemSpawns[i] =
                    new ExoItemSpawner(
                            ExoPickupEnum.get(Integer.parseInt(ffaItemSpawnStrings[i * 4])),
                                    Float.parseFloat(ffaItemSpawnStrings[(i * 4) + 1]),
                            Float.parseFloat(ffaItemSpawnStrings[(i * 4) + 2]),
                                    Float.parseFloat(ffaItemSpawnStrings[(i * 4) + 3]));
        }

        List<ExoLineSegment> coordSegments = new ArrayList<>();
        for (ExoTriangleVertices verts : triangles) {
            Exo2DVector v1 = modifiedCoords[verts.vertexOne].discardZ();
            Exo2DVector v2 = modifiedCoords[verts.vertexTwo].discardZ();
            Exo2DVector v3 = modifiedCoords[verts.vertexThree].discardZ();

            addWithSieve(new ExoLineSegment(v1, v2), coordSegments);
            addWithSieve(new ExoLineSegment(v1, v3), coordSegments);
            addWithSieve(new ExoLineSegment(v2, v3), coordSegments);
        }

        lineSegments = coordSegments.toArray(new ExoLineSegment[0]);
    }

    private static void addWithSieve(ExoLineSegment segment, List<ExoLineSegment> segments) {
        if (!segment.isDegenerate()) {
            for (int i = 0; i < segments.size(); i++) {
                if (segments.get(i).equals(segment)) return;
            }
            segments.add(segment);
        }
    }

    private static ExoInt2DVector getInt2DVector(String[] pairStrings) {
        return new ExoInt2DVector(
                Integer.parseInt(pairStrings[0]), Integer.parseInt(pairStrings[1]));
    }

    private static Exo3DVector get3DVector(String[] tripletStrings) {
        return new Exo3DVector(
                Float.parseFloat(tripletStrings[0]),
                Float.parseFloat(tripletStrings[1]),
                Float.parseFloat(tripletStrings[2]));
    }

    private static String getProperty(String[] properties, String property) {
        for (String s : properties) {
            if (s.startsWith(property)) return s.substring(s.indexOf(':') + 2);
        }
        System.err.println("No such property " + property + "!");
        System.exit(1);
        return null;
    }

    public Image getImage(
            float scalar, ExoInt2DVector scaledDrawTranslate, ExoInt2DVector scaledDrawSize) {
        BufferedImage drawImage =
                new BufferedImage(scaledDrawSize.x, scaledDrawSize.y, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = drawImage.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, scaledDrawSize.x, scaledDrawSize.y);

        g2d.translate(scaledDrawTranslate.x, scaledDrawTranslate.y);

        g2d.setColor(Color.BLACK);
        for (ExoTriangleVertices triangle : triangles) {
            ExoInt2DVector pointOne =
                    modifiedCoords[triangle.vertexOne].convertNativeToDraw(scalar);
            ExoInt2DVector pointTwo =
                    modifiedCoords[triangle.vertexTwo].convertNativeToDraw(scalar);
            ExoInt2DVector pointThree =
                    modifiedCoords[triangle.vertexThree].convertNativeToDraw(scalar);

            g2d.drawLine(pointOne.x, pointOne.y, pointTwo.x, pointTwo.y);
            g2d.drawLine(pointTwo.x, pointTwo.y, pointThree.x, pointThree.y);
            g2d.drawLine(pointThree.x, pointThree.y, pointOne.x, pointOne.y);
        }

        g2d.translate(-scaledDrawTranslate.x, -scaledDrawTranslate.y);

        return drawImage;
    }

    public ExoInt2DVector getDrawTranslate(float scalar) {
        return drawTranslate.scale(scalar);
    }

    public ExoInt2DVector getDrawSize(float scalar) {
        return drawSize.scale(scalar);
    }

    public FixtureDef[] getWallFixtures() {
        FixtureDef[] retVal = new FixtureDef[lineSegments.length];

        for (int i = 0; i < retVal.length; i++) {
            EdgeShape shape = new EdgeShape();
            shape.set(
                    new Vector2(lineSegments[i].vertexOne.x, lineSegments[i].vertexOne.y),
                    new Vector2(lineSegments[i].vertexTwo.x, lineSegments[i].vertexTwo.y));
            retVal[i] = new FixtureDef();
            retVal[i].shape = shape;
        }

        return retVal;
    }

    public Exo2DVector[] getTeamPlayerSpawns() {
        return teamPlayerSpawns;
    }

    public Exo2DVector[] getFFAPlayerSpawns() {
        return ffaPlayerSpawns;
    }

    public ExoItemSpawner[] getTeamItemSpawns() {
        return teamItemSpawns;
    }

    public ExoItemSpawner[] getFFAItemSpawns() {
        return ffaItemSpawns;
    }
}
