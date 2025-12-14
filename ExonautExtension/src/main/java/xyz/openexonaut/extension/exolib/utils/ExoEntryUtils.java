package xyz.openexonaut.extension.exolib.utils;

import java.util.*;

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
import xyz.openexonaut.extension.exolib.resources.*;

public final class ExoEntryUtils {
    private static final RoomVariable initialImbalance = new SFSRoomVariable("imbalance", (int) 0);
    private static final RoomVariable initialJoinedTegIDs = new SFSRoomVariable("joinedTegIDs", "");

    static {
        initialImbalance.setHidden(true);
        initialJoinedTegIDs.setHidden(true);
    }

    public static final List<RoomVariable> initialRoomVars =
            List.of(
                    new SFSRoomVariable("state", "wait_for_min_players"),
                    initialImbalance,
                    initialJoinedTegIDs,
                    new SFSRoomVariable("time", (int) 0));
    public static final List<UserVariable> initialUserVars =
            List.of(
                    new SFSUserVariable("clientState", "ready"),
                    new SFSUserVariable("avatarState", "halted"),
                    new SFSUserVariable("hacks", (int) 0),
                    new SFSUserVariable("capturedMethod", (int) 0),
                    new SFSUserVariable("capturedBy", (int) 0),
                    new SFSUserVariable("pickingup", true),
                    new SFSUserVariable("boost", (int) (-1)),
                    new SFSUserVariable("teamBoost", (int) (-1)),
                    new SFSUserVariable("x", (double) 0.0),
                    new SFSUserVariable("y", (double) 0.0),
                    new SFSUserVariable("armAngle", (double) 0.0),
                    new SFSUserVariable("faceTargetDir", (double) 0.0),
                    new SFSUserVariable("inactive", false),
                    new SFSUserVariable("moveState", (int) 0),
                    new SFSUserVariable("moveDir", (int) 0));

    private ExoEntryUtils() {}

    // return value: success
    public static boolean login(Session session, String username, String password, Zone zone) {
        // empty string is guest login
        if (username.equals("")) {
            session.setProperty("dname", "");
            session.setProperty("tegid", "");
        } else {
            String displayName = ExoDB.checkLogin(session, username, password);

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
            level = ExoDB.getPlayerLevel(tegid);
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
        List<UserVariable> userVars = new ArrayList<>();
        ExoPlayer player = (ExoPlayer) sender.getProperty("ExoPlayer");
        boolean team = modeParam.equals("team");
        boolean banzai = sender.getVariable("faction").getStringValue().equals("banzai");
        String mode = team ? "team" : "freeforall";
        ExoSuit suit = ExoGameData.getSuit(sender.getVariable("suitId").getIntValue());
        MatchExpression roomMatch = new MatchExpression("mode", StringMatch.EQUALS, mode);

        // nickName is prior set
        // level is prior set
        // myMapId is provided
        // lastMapLoadedId is provided
        // clientVersion is provided
        // faction is provided
        // suitId is provided
        // weaponId is provided
        // xp is provided

        userVars.addAll(initialUserVars);
        userVars.add(new SFSUserVariable("health", (double) suit.Health));
        sfsApi.setUserVariables(sender, userVars);

        player.setSuit(suit);

        if (team) {
            roomMatch =
                    roomMatch.and(
                            "imbalance",
                            banzai ? NumberMatch.LESS_THAN : NumberMatch.GREATER_THAN,
                            banzai ? 1 : -1);
        }

        List<Room> allRooms = sfsApi.findRooms(zone.getRoomList(), roomMatch, 0);
        if (allRooms.size() > 0) {
            List<Room> previousRooms =
                    sfsApi.findRooms(
                            allRooms,
                            new MatchExpression(
                                            RoomProperties.HAS_FREE_PLAYER_SLOTS,
                                            BoolMatch.EQUALS,
                                            true)
                                    .and(
                                            "joinedTegIDs",
                                            StringMatch.CONTAINS,
                                            String.format(
                                                    "%s_", (String) sender.getProperty("tegid"))),
                            0);

            List<Room> roomList = previousRooms.size() > 0 ? previousRooms : allRooms;
            try {
                sfsApi.joinRoom(sender, roomList.get((int) (Math.random() * roomList.size())));
            } catch (SFSJoinRoomException e) {
                throw new RuntimeException(e);
            }
        } else {
            CreateRoomSettings roomSettings = new CreateRoomSettings();
            List<RoomVariable> roomVars = new ArrayList<>();
            int mapId = (int) (Math.random() * ExoMapManager.getMapCount()) + 1;

            roomSettings.setMaxUsers(8);
            roomSettings.setMaxVariablesAllowed(10);
            roomSettings.setDynamic(true);
            roomSettings.setAutoRemoveMode(SFSRoomRemoveMode.WHEN_EMPTY);
            roomSettings.setExtension(
                    new CreateRoomSettings.RoomExtensionSettings(
                            "Exonaut", "xyz.openexonaut.extension.room.ExonautRoomExtension"));
            roomSettings.setGame(true);

            // "stop" is used existentially
            roomVars.addAll(initialRoomVars);
            roomVars.add(
                    new SFSRoomVariable(
                            "hackLimit",
                            team ? ExoProps.getMaxHacksTeam() : ExoProps.getMaxHacksSolo()));
            roomVars.add(new SFSRoomVariable("mapId", mapId));
            roomVars.add(new SFSRoomVariable("lastMapLoadedId", mapId));
            roomVars.add(new SFSRoomVariable("mode", mode));

            roomSettings.setName(
                    String.format("%s_%d", team ? "team" : "ffa", System.currentTimeMillis()));
            roomSettings.setRoomVariables(roomVars);

            try {
                sfsApi.createRoom(zone, roomSettings, sender, true, null);
            } catch (SFSCreateRoomException e) {
                throw new RuntimeException(e);
            }
        }

        ISFSObject responseParams = new SFSObject();
        responseParams.putUtfString("roomName", sender.getLastJoinedRoom().getName());
        zone.getExtension().send("findRoom", responseParams, sender);
    }
}
