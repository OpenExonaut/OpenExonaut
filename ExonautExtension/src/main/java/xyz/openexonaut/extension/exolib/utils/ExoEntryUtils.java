package xyz.openexonaut.extension.exolib.utils;

import java.util.*;

import org.bson.*;

import com.smartfoxserver.bitswarm.sessions.*;
import com.smartfoxserver.v2.*;
import com.smartfoxserver.v2.api.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.entities.match.*;
import com.smartfoxserver.v2.entities.variables.*;
import com.smartfoxserver.v2.exceptions.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.messages.*;

public abstract class ExoEntryUtils {
    // return value: success
    public static boolean login(Session session, String username, String password, Zone zone) {
        // empty string is guest login
        if (username.equals("")) {
            session.setProperty("dname", "");
            session.setProperty("tegid", "");
        } else {
            String displayName =
                    (String)
                            zone.getExtension()
                                    .handleInternalMessage(
                                            "checkLogin",
                                            new ExoLoginParameters(session, username, password));

            if (displayName == null) {
                return false;
            }

            session.setProperty("dname", displayName);
            session.setProperty("tegid", username);
        }

        return true;
    }

    public static void initUser(User user, Zone zone) {
        String tegid = (String) user.getSession().getProperty("tegid");
        String displayName = (String) user.getSession().getProperty("dname");
        int level = 1;

        user.setProperty("tegid", tegid);
        user.setProperty("ExoPlayer", new ExoPlayer(user));

        if (tegid.equals("")) {
            displayName = user.getName();
        } else {
            level =
                    ((Document) zone.getExtension().handleInternalMessage("getPlayerObject", tegid))
                            .getInteger("Level");
        }

        SmartFoxServer.getInstance()
                .getAPIManager()
                .getSFSApi()
                .setUserVariables(
                        user,
                        List.of(
                                new SFSUserVariable("level", level),
                                new SFSUserVariable("nickName", displayName)));
    }

    public static void findRoom(User sender, String modeParam, Zone zone) {
        ISFSApi sfsApi = SmartFoxServer.getInstance().getAPIManager().getSFSApi();
        CreateRoomSettings roomSettings = new CreateRoomSettings();
        List<RoomVariable> roomVars = new ArrayList<>();
        List<UserVariable> userVars = new ArrayList<>();
        int mapId =
                (int)
                                (Math.random()
                                        * (int)
                                                zone.getExtension()
                                                        .handleInternalMessage("getMapCount", null))
                        + 1;
        ExoPlayer player = (ExoPlayer) sender.getProperty("ExoPlayer");
        String mode;

        ExoSuit suit =
                (ExoSuit)
                        zone.getExtension()
                                .handleInternalMessage(
                                        "getSuit", sender.getVariable("suitId").getIntValue());

        // nickName is prior set
        // level is prior set
        // myMapId is provided
        // lastMapLoadedId is provided
        // clientVersion is provided
        // faction is provided
        // suitId is provided
        // weaponId is provided
        // xp is provided
        ensureVariable(sender, userVars, "clientState", "ready");
        ensureVariable(sender, userVars, "avatarState", "halted");
        ensureVariable(sender, userVars, "hacks", (int) 0);
        ensureVariable(sender, userVars, "capturedMethod", (int) 0);
        ensureVariable(sender, userVars, "capturedBy", (int) 0);
        ensureVariable(sender, userVars, "pickingup", true);
        ensureVariable(sender, userVars, "boost", (int) 0);
        ensureVariable(sender, userVars, "teamBoost", (int) 0);
        ensureVariable(sender, userVars, "x", (double) 0.0);
        ensureVariable(sender, userVars, "y", (double) 0.0);
        ensureVariable(sender, userVars, "health", (double) suit.Health);
        ensureVariable(sender, userVars, "armAngle", (double) 0.0);
        ensureVariable(sender, userVars, "faceTargetDir", (double) 0.0);
        ensureVariable(sender, userVars, "inactive", false);
        ensureVariable(sender, userVars, "moveState", (int) 0);
        ensureVariable(sender, userVars, "moveDir", (int) 0);

        if (userVars.size() > 0) {
            sfsApi.setUserVariables(sender, userVars);
        }

        player.setSuit(suit);

        roomSettings.setMaxUsers(8);
        roomSettings.setMaxVariablesAllowed(10);
        roomSettings.setDynamic(true);
        roomSettings.setAutoRemoveMode(SFSRoomRemoveMode.WHEN_EMPTY);
        roomSettings.setExtension(
                new CreateRoomSettings.RoomExtensionSettings(
                        "Exonaut", "xyz.openexonaut.extension.room.ExonautRoomExtension"));
        roomSettings.setGame(true);

        // "stop" is used existentially
        roomVars.add(new SFSRoomVariable("state", "wait_for_min_players"));
        roomVars.add(new SFSRoomVariable("mapId", mapId));
        roomVars.add(new SFSRoomVariable("lastMapLoadedId", mapId));
        roomVars.add(new SFSRoomVariable("hackLimit", (int) 20));
        roomVars.add(new SFSRoomVariable("time", (int) 0));

        if (modeParam.equals("team")) {
            roomSettings.setName(String.format("team_%d", System.currentTimeMillis()));
            roomVars.add(new SFSRoomVariable("mode", "team"));
            mode = "team";
        } else {
            roomSettings.setName(String.format("ffa_%d", System.currentTimeMillis()));
            roomVars.add(new SFSRoomVariable("mode", "freeforall"));
            mode = "freeforall";
        }

        roomSettings.setRoomVariables(roomVars);

        try {
            sfsApi.quickJoinOrCreateRoom(
                    sender,
                    new MatchExpression("mode", StringMatch.EQUALS, mode)
                            .and("state", StringMatch.NOT_EQUALS, "play"),
                    Arrays.asList("default"),
                    roomSettings);
        } catch (SFSCreateRoomException | SFSJoinRoomException e) {
            throw new RuntimeException(e);
        }

        ISFSObject responseParams = new SFSObject();
        responseParams.putUtfString("roomName", sender.getLastJoinedRoom().getName());
        zone.getExtension().send("findRoom", responseParams, sender);
    }

    private static void ensureVariable(
            User user, List<UserVariable> variables, String id, Object defaultValue) {
        if (!user.containsVariable(id)) {
            variables.add(new SFSUserVariable(id, defaultValue));
        }
    }
}
