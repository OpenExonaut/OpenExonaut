package xyz.openexonaut.extension.zone;

import java.io.*;
import java.net.*;
import java.util.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.*;

import com.smartfoxserver.bitswarm.sessions.*;
import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.exolib.geo.*;
import xyz.openexonaut.extension.zone.eventhandlers.*;
import xyz.openexonaut.extension.zone.reqhandlers.*;

public class ExonautZoneExtension extends SFSExtension {
    private Properties props = null;
    private JsonNode gameData = null;
    private MapLoader[] mapLoaders = new MapLoader[9];
    private MongoClient mongoClient = null;
    private MongoDatabase database = null;
    private MongoCollection<Document> userCollection = null;

    private ExoMap[] maps = new ExoMap[9];
    private ExoWeapon[] weapons = null;
    private ExoSuit[] suits = null;

    private boolean debugGFX = false;
    private float debugGFXScale = 1f;

    @Override
    public void init() {
        props = getConfigProperties();
        if (!props.containsKey("httpURI")) {
            throw new RuntimeException(
                    "HTTP server URI not set. Please create config.properties in the extension folder and define it.");
        }

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

        for (int i = 0; i < 9; i++) {
            mapLoaders[i] =
                    new MapLoader(
                            getCurrentFolder()
                                    + "worlds/world_"
                                    + (i + 1)); // world 0 exists, but is the tutorial world, and
            // the tutorial is always run locally

            if (debugGFX) {
                ExoInt2DVector scaledDrawTranslate = mapLoaders[i].getDrawTranslate(debugGFXScale);
                ExoInt2DVector scaledDrawSize = mapLoaders[i].getDrawSize(debugGFXScale);
                maps[i] =
                        new ExoMap(
                                mapLoaders[i].getWallFixtures(),
                                mapLoaders[i].getTeamPlayerSpawns(),
                                mapLoaders[i].getFFAPlayerSpawns(),
                                mapLoaders[i].getTeamItemSpawns(),
                                mapLoaders[i].getFFAItemSpawns(),
                                mapLoaders[i].getImage(
                                        debugGFXScale, scaledDrawTranslate, scaledDrawSize),
                                scaledDrawTranslate,
                                scaledDrawSize,
                                debugGFXScale);
            } else {
                maps[i] =
                        new ExoMap(
                                mapLoaders[i].getWallFixtures(),
                                mapLoaders[i].getTeamPlayerSpawns(),
                                mapLoaders[i].getFFAPlayerSpawns(),
                                mapLoaders[i].getTeamItemSpawns(),
                                mapLoaders[i].getFFAItemSpawns(),
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

    private JsonNode getFullUserObject(String tegid) {
        Document userJSON = userCollection.find(Filters.eq("user.TEGid", tegid)).first();
        if (userJSON != null) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = null;
            try {
                data = mapper.readTree(userJSON.toJson());
                return data;
            } catch (IOException e) {
                trace(ExtensionLogLevel.ERROR, e);
            }
        }
        return null;
    }

    // returns: dname if valid non-guest login, null otherwise; input password is encrypted by SFS2X
    private String checkLogin(Session session, String username, String password) {
        JsonNode userObject = getFullUserObject(username);
        if (userObject != null) {
            if (getApi().checkSecurePassword(
                            session, userObject.get("user").get("authid").asText(), password)) {
                return userObject.get("user").get("dname").asText();
            }
        }
        return null;
    }

    private JsonNode getPlayerObject(String tegid) {
        JsonNode userObject = getFullUserObject(tegid);
        if (userObject != null) {
            return userObject.get("player");
        }
        return null;
    }

    @Override
    public Object handleInternalMessage(String command, Object parameters) {
        switch (command) {
            case "getMap":
                return maps[(Integer) parameters - 1];
            case "getSuits":
                return suits;
            case "getWeapons":
                return weapons;
            case "checkLogin":
                Object[] loginArgs = (Object[]) parameters;
                return checkLogin(
                        (Session) loginArgs[0], (String) loginArgs[1], (String) loginArgs[2]);
            case "getPlayerObject":
                return getPlayerObject((String) parameters);
            default:
                trace(ExtensionLogLevel.ERROR, "Invalid internal message " + command);
                return null;
        }
    }
}
