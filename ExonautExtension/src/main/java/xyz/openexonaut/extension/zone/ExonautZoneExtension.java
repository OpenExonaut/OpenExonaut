package xyz.openexonaut.extension.zone;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import com.fasterxml.jackson.databind.*;

import com.smartfoxserver.v2.*;
import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.resources.*;
import xyz.openexonaut.extension.zone.eventhandlers.*;
import xyz.openexonaut.extension.zone.reqhandlers.*;

public class ExonautZoneExtension extends SFSExtension implements Runnable {
    private Properties props = null;
    private String propertiesPath = "";
    private ScheduledFuture<?> propsHandle = null;

    @Override
    public void init() {
        props = getConfigProperties();
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
                Math.max(0f, Float.parseFloat(props.getProperty("debugGFXScale"))));

        int configReload = Integer.parseInt(props.getProperty("configReload"));
        propertiesPath = String.format("extensions/%s/%s", getName(), getPropertiesFileName());
        propsHandle =
                SmartFoxServer.getInstance()
                        .getTaskScheduler()
                        .scheduleAtFixedRate(this, configReload, configReload, TimeUnit.SECONDS);

        addRequestHandler("findRoom", FindRoomReqHandler.class);

        addEventHandler(SFSEventType.USER_LOGIN, UserLoginHandler.class);
        addEventHandler(SFSEventType.USER_LOGOUT, UserLogoutHandler.class);
        addEventHandler(SFSEventType.USER_JOIN_ZONE, UserJoinZoneHandler.class);

        trace(ExtensionLogLevel.INFO, "Exonaut Zone Extension init finished");
    }

    @Override
    public void run() {
        Properties newProps = new Properties();
        try {
            newProps.load(new FileInputStream(propertiesPath));
        } catch (Exception e) {
            logger.error("props reload error", e);
            return;
        }
        if (props == null || !newProps.equals(props)) {
            props = newProps;
            ExoProps.init(props);
            trace(ExtensionLogLevel.INFO, "Reloaded properties.");
        }
    }

    @Override
    public void destroy() {
        if (propsHandle != null) {
            propsHandle.cancel(false);
        }
        ExoDB.destroy();
        super.destroy();
    }
}
