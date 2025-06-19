package xyz.openexonaut.extension.zone;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import com.fasterxml.jackson.databind.*;
import org.bson.*;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.map.*;
import xyz.openexonaut.extension.exolib.messages.*;
import xyz.openexonaut.extension.exolib.resources.*;
import xyz.openexonaut.extension.zone.eventhandlers.*;
import xyz.openexonaut.extension.zone.reqhandlers.*;

public class ExonautZoneExtension extends SFSExtension {
    private ExoProps exoProps = null;
    private ExoDB database = null;
    private ExoGameData gameData = null;
    private ExoMapManager mapManager = null;

    @Override
    public void init() {
        Properties props = getConfigProperties();
        if (!props.containsKey("httpURI")) {
            throw new RuntimeException(
                    "HTTP server URI not set. Please create config.properties in the extension folder and define it.");
        }

        try {
            gameData =
                    new ExoGameData(
                            new ObjectMapper()
                                    .readTree(
                                            URI.create(
                                                            String.format(
                                                                    "%s/exonaut/gamedata.json",
                                                                    props.getProperty("httpURI")))
                                                    .toURL()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        exoProps = new ExoProps(props);
        database = new ExoDB(props.getProperty("mongoURI"));
        mapManager =
                new ExoMapManager(
                        Paths.get(getCurrentFolder(), "worlds"),
                        Integer.parseInt(props.getProperty("mapCount")),
                        Boolean.parseBoolean(props.getProperty("debugGFX"))
                                ? Float.parseFloat(props.getProperty("debugGFXScale"))
                                : 0f);

        addRequestHandler("findRoom", FindRoomReqHandler.class);

        addEventHandler(SFSEventType.USER_LOGIN, UserLoginHandler.class);
        addEventHandler(SFSEventType.USER_JOIN_ZONE, UserJoinZoneHandler.class);

        trace(ExtensionLogLevel.INFO, "Exonaut Zone Extension init finished");
    }

    @Override
    public void destroy() {
        mapManager.destroy();
        database.destroy();
        super.destroy();
    }

    private ExoMap getMap(int mapId) {
        return mapManager.getMap(mapId);
    }

    private ExoSuit getSuit(int id) {
        return gameData.getSuit(id);
    }

    private ExoGameData getGameData() {
        return gameData;
    }

    private String checkLogin(ExoLoginParameters loginArgs) {
        return database.checkLogin(loginArgs.session, loginArgs.username, loginArgs.password);
    }

    private Document getPlayerObject(String tegid) {
        return database.getPlayerObject(tegid);
    }

    private ExoProps getProps() {
        return exoProps;
    }

    private int getMapCount() {
        return mapManager.getMapCount();
    }

    @Override
    public Object handleInternalMessage(String command, Object parameters) {
        switch (command) {
            case "getMap":
                return getMap((Integer) parameters);
            case "getSuit":
                return getSuit((Integer) parameters);
            case "getGameData":
                return getGameData();
            case "checkLogin":
                return checkLogin((ExoLoginParameters) parameters);
            case "getPlayerObject":
                return getPlayerObject((String) parameters);
            case "getProps":
                return getProps();
            case "getMapCount":
                return getMapCount();
            default:
                throw new RuntimeException(
                        String.format("Invalid internal zone message %s", command));
        }
    }
}
