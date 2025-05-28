package xyz.openexonaut.extension.zone;

import java.io.*;
import java.net.*;
import java.util.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.*;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.geo.*;
import xyz.openexonaut.extension.exolib.map.*;
import xyz.openexonaut.extension.zone.eventhandlers.*;
import xyz.openexonaut.extension.zone.loader.*;
import xyz.openexonaut.extension.zone.messages.*;
import xyz.openexonaut.extension.zone.reqhandlers.*;

public class ExonautZoneExtension extends SFSExtension {
    private Properties props = null;
    private JsonNode gameData = null;
    private MongoClient mongoClient = null;
    private MongoDatabase database = null;
    private MongoCollection<Document> userCollection = null;

    private int mapCount = 0;
    private ExoMap[] maps = null;
    private ExoWeapon[] weapons = null;
    private ExoSuit[] suits = null;

    private boolean debugGFX = false;
    private float debugGFXScale = 1f;

    private ExoProps exoProps = null;

    @Override
    public void init() {
        props = getConfigProperties();
        if (!props.containsKey("httpURI")) {
            throw new RuntimeException(
                    "HTTP server URI not set. Please create config.properties in the extension folder and define it.");
        }
        exoProps = new ExoProps(props);

        try {
            gameData =
                    new ObjectMapper()
                            .readTree(
                                    URI.create(
                                                    props.getProperty("httpURI")
                                                            + "/exonaut/gamedata.json")
                                            .toURL());
            mongoClient = MongoClients.create(props.getProperty("mongoURI"));
            database = mongoClient.getDatabase("openexonaut");
            userCollection = database.getCollection("users");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        debugGFX = Boolean.parseBoolean(props.getProperty("debugGFX"));
        debugGFXScale = Float.parseFloat(props.getProperty("debugGFXScale"));
        mapCount = Integer.parseInt(props.getProperty("mapCount"));

        maps = new ExoMap[mapCount];
        for (int i = 0; i < mapCount; i++) {
            MapLoader mapLoader =
                    new MapLoader(
                            getCurrentFolder()
                                    + "worlds/world_"
                                    + (i + 1)); // world 0 exists, but is the tutorial world, and
            // the tutorial is always run locally

            if (debugGFX) {
                ExoInt2DVector scaledDrawTranslate = mapLoader.getDrawTranslate(debugGFXScale);
                ExoInt2DVector scaledDrawSize = mapLoader.getDrawSize(debugGFXScale);
                maps[i] =
                        new ExoMap(
                                mapLoader.getWallFixtureDefs(),
                                mapLoader.getTeamPlayerSpawns(),
                                mapLoader.getFFAPlayerSpawns(),
                                mapLoader.getTeamItemSpawns(),
                                mapLoader.getFFAItemSpawns(),
                                mapLoader.getImage(
                                        debugGFXScale, scaledDrawTranslate, scaledDrawSize),
                                scaledDrawTranslate,
                                scaledDrawSize,
                                debugGFXScale);
            } else {
                maps[i] =
                        new ExoMap(
                                mapLoader.getWallFixtureDefs(),
                                mapLoader.getTeamPlayerSpawns(),
                                mapLoader.getFFAPlayerSpawns(),
                                mapLoader.getTeamItemSpawns(),
                                mapLoader.getFFAItemSpawns(),
                                null,
                                null,
                                null,
                                0f);
            }
        }

        ArrayNode weaponData = (ArrayNode) gameData.get("weapons");
        weapons = new ExoWeapon[weaponData.size()];
        for (int i = 0; i < weapons.length; i++) {
            weapons[i] = new ExoWeapon(weaponData.get(i));
        }

        ArrayNode modData = (ArrayNode) gameData.get("mods");
        ExoMod[] mods = new ExoMod[modData.size()];
        for (int i = 0; i < mods.length; i++) {
            mods[i] = new ExoMod(modData.get(i), weapons);
        }

        ArrayNode suitData = (ArrayNode) gameData.get("suits");
        suits = new ExoSuit[suitData.size()];
        for (int i = 0; i < suits.length; i++) {
            suits[i] = new ExoSuit(suitData.get(i), mods);
        }

        addRequestHandler("findRoom", FindRoomReqHandler.class);

        addEventHandler(SFSEventType.USER_LOGIN, UserLoginHandler.class);
        addEventHandler(SFSEventType.USER_JOIN_ZONE, UserJoinZoneHandler.class);

        trace(ExtensionLogLevel.INFO, "Exonaut Zone Extension init finished");
    }

    @Override
    public void destroy() {
        for (ExoMap map : maps) {
            if (map != null) {
                map.destroy();
            }
        }

        mongoClient.close();

        super.destroy();
    }

    private Document getFullUserObject(String tegid) {
        return userCollection.find(Filters.eq("user.TEGid", tegid)).first();
    }

    private ExoMap getMap(int mapId) {
        return maps[mapId - 1];
    }

    private ExoSuit[] getSuits() {
        return suits;
    }

    private ExoWeapon[] getWeapons() {
        return weapons;
    }

    // returns: dname if valid non-guest login, null otherwise; input password is encrypted by SFS2X
    private String checkLogin(ExoLoginParameters loginArgs) {
        Document userObject = getFullUserObject(loginArgs.username);
        if (userObject != null) {
            if (getApi().checkSecurePassword(
                            loginArgs.session,
                            userObject.get("user", Document.class).getString("authid"),
                            loginArgs.password)) {
                return userObject.get("user", Document.class).getString("dname");
            }
        }
        return null;
    }

    private Document getPlayerObject(String tegid) {
        Document userObject = getFullUserObject(tegid);
        if (userObject != null) {
            return userObject.get("player", Document.class);
        }
        return null;
    }

    private ExoProps getProps() {
        return exoProps;
    }

    private int getMapCount() {
        return mapCount;
    }

    @Override
    public Object handleInternalMessage(String command, Object parameters) {
        switch (command) {
            case "getMap":
                return getMap((Integer) parameters);
            case "getSuits":
                return getSuits();
            case "getWeapons":
                return getWeapons();
            case "checkLogin":
                return checkLogin((ExoLoginParameters) parameters);
            case "getPlayerObject":
                return getPlayerObject((String) parameters);
            case "getProps":
                return getProps();
            case "getMapCount":
                return getMapCount();
            default:
                trace(ExtensionLogLevel.ERROR, "Invalid internal message " + command);
                return null;
        }
    }
}
