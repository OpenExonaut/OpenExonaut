package xyz.openexonaut.extension.zone;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import com.fasterxml.jackson.databind.*;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.resources.*;
import xyz.openexonaut.extension.zone.eventhandlers.*;
import xyz.openexonaut.extension.zone.reqhandlers.*;

public class ExonautZoneExtension extends SFSExtension {
    @Override
    public void init() {
        Properties props = getConfigProperties();
        if (!props.containsKey("httpURI")) {
            throw new RuntimeException(
                    "HTTP server URI not set. Please create config.properties in the extension folder and define it.");
        }

        try {
            ExoGameData.init(
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

        ExoProps.init(props);
        ExoDB.init(props.getProperty("mongoURI"));
        ExoMapManager.init(
                Paths.get(getCurrentFolder(), "worlds"),
                Integer.parseInt(props.getProperty("mapCount")),
                Boolean.parseBoolean(props.getProperty("debugGFX"))
                        ? Math.max(0f, Float.parseFloat(props.getProperty("debugGFXScale")))
                        : 0f);
        ExoDefs.init();

        addRequestHandler("findRoom", FindRoomReqHandler.class);

        addEventHandler(SFSEventType.USER_LOGIN, UserLoginHandler.class);
        addEventHandler(SFSEventType.USER_JOIN_ZONE, UserJoinZoneHandler.class);

        trace(ExtensionLogLevel.INFO, "Exonaut Zone Extension init finished");
    }

    @Override
    public void destroy() {
        ExoMapManager.destroy();
        ExoDB.destroy();
        ExoDefs.destroy();
        super.destroy();
    }
}
