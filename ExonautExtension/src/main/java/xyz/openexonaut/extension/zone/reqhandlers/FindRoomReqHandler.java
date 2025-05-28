package xyz.openexonaut.extension.zone.reqhandlers;

import java.util.*;

import com.smartfoxserver.v2.api.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.entities.match.*;
import com.smartfoxserver.v2.entities.variables.*;
import com.smartfoxserver.v2.exceptions.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.game.*;

public class FindRoomReqHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        CreateRoomSettings roomSettings = new CreateRoomSettings();
        List<RoomVariable> roomVars = new ArrayList<>();
        List<UserVariable> userVars = new ArrayList<>();
        int mapId =
                (int)
                                (Math.random()
                                        * (int)
                                                getParentExtension()
                                                        .handleInternalMessage("getMapCount", null))
                        + 1;
        String mode;

        ExoSuit suit =
                ((ExoSuit[]) getParentExtension().handleInternalMessage("getSuits", null))
                        [sender.getVariable("suitId").getIntValue() - 1];

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
            getApi().setUserVariables(sender, userVars);
        }

        ((ExoPlayer) sender.getProperty("ExoPlayer")).setSuit(suit);

        roomSettings.setMaxUsers(8);
        roomSettings.setMaxVariablesAllowed(50);
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

        if (params.getUtfString("mode").equals("team")) {
            roomSettings.setName("team_" + System.currentTimeMillis());
            roomVars.add(new SFSRoomVariable("mode", "team"));
            mode = "team";
        } else {
            roomSettings.setName("ffa_" + System.currentTimeMillis());
            roomVars.add(new SFSRoomVariable("mode", "freeforall"));
            mode = "freeforall";
        }

        roomSettings.setRoomVariables(roomVars);

        try {
            getApi().quickJoinOrCreateRoom(
                            sender,
                            new MatchExpression("mode", StringMatch.EQUALS, mode)
                                    .and("state", StringMatch.NOT_EQUALS, "play"),
                            Arrays.asList("default"),
                            roomSettings);
        } catch (SFSCreateRoomException e) {
            trace(ExtensionLogLevel.ERROR, "Create room exception: ", e);
        } catch (SFSJoinRoomException e) {
            trace(ExtensionLogLevel.ERROR, "Join room exception: ", e);
        }

        ISFSObject responseParams = new SFSObject();
        responseParams.putUtfString("roomName", sender.getLastJoinedRoom().getName());
        send("findRoom", responseParams, sender);
    }

    private static void ensureVariable(
            User user, List<UserVariable> variables, String id, Object defaultValue) {
        if (!user.containsVariable(id)) {
            variables.add(new SFSUserVariable(id, defaultValue));
        }
    }
}
