package xyz.openexonaut.extension.zone;

import java.io.*;
import java.net.*;
import java.util.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.exolib.geo.*;

public class ExonautZoneExtension extends SFSExtension {
    private Properties props = null;
    private JsonNode gameData = null;
    private MapLoader[] mapLoaders = new MapLoader[9];

    private ExoMap[] maps = new ExoMap[9];
    private ExoWeapon[] weapons = null;
    private ExoSuit[] suits = null;

    private boolean debugGFX = false;
    private float debugGFXScale = 1;

    @Override
    public void init () {
        props = getConfigProperties();
        if (!props.containsKey("httpURI")) {
            throw new RuntimeException(
                "HTTP server URI not set. Please create config.properties in the extension folder and define it.");
        }

        try {
            gameData = new ObjectMapper().readTree(new URL(props.getProperty("httpURI") + "/exonaut/gamedata.json"));
        } catch (IOException e) {
            trace(ExtensionLogLevel.ERROR, e);
        }

        debugGFX = Boolean.parseBoolean(props.getProperty("debugGFX"));
        debugGFXScale = Float.parseFloat(props.getProperty("debugGFXScale"));

        for (int i = 0; i < 9; i++) {
            mapLoaders[i] = new MapLoader(getCurrentFolder() + "worlds/world_" + (i + 1)); // world 0 exists, but is the tutorial world, and the tutorial is always run locally

            if (debugGFX) {
                ExoInt2DVector scaledDrawTranslate = mapLoaders[i].getDrawTranslate(debugGFXScale);
                ExoInt2DVector scaledDrawSize = mapLoaders[i].getDrawSize(debugGFXScale);
                maps[i] = new ExoMap(mapLoaders[i].getImage(debugGFXScale, scaledDrawTranslate, scaledDrawSize), scaledDrawTranslate, scaledDrawSize);
            }
            else {
                maps[i] = new ExoMap(null, null, null);
            }
        }

        ArrayNode weaponData = (ArrayNode)gameData.get("weapons");
        weapons = new ExoWeapon[weaponData.size()];
        for (int i = 0; i < weapons.length; i++) {
            weapons[i] = new ExoWeapon(weaponData.get(i));
        }

        ArrayNode modData = (ArrayNode)gameData.get("mods");
        ExoMod[] mods = new ExoMod[modData.size()];
        for (int i = 0; i < mods.length; i++) {
            mods[i] = new ExoMod(modData.get(i), weapons);
        }

        ArrayNode suitData = (ArrayNode)gameData.get("suits");
        suits = new ExoSuit[suitData.size()];
        for (int i = 0; i < suits.length; i++) {
            suits[i] = new ExoSuit(suitData.get(i), mods);
        }

        addRequestHandler("findRoom", FindRoomReqHandler.class);

        trace(ExtensionLogLevel.INFO, "Exonaut Zone Extension init finished");
    }

    @Override
    public Object handleInternalMessage (String command, Object parameters) {
        switch (command) {
            case "getMap":
                return maps[(Integer)parameters - 1];
            case "getSuits":
                return suits;
            case "getWeapons":
                return weapons;
            default:
                trace(ExtensionLogLevel.ERROR, "Invalid internal message " + command);
                return null;
        }
    }
}
