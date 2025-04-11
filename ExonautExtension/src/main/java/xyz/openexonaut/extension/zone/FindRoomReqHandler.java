package xyz.openexonaut.extension.zone;

import java.util.*;

import com.smartfoxserver.v2.api.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.entities.match.*;
import com.smartfoxserver.v2.entities.variables.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.*;

public class FindRoomReqHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        try {
            CreateRoomSettings roomSettings = new CreateRoomSettings();
            List<RoomVariable> roomVars = new ArrayList<>();
            List<UserVariable> userVars = new ArrayList<>();
            int mapId = (int) (Math.random() * 9 + 1);
            String mode;

            userVars.add(new SFSUserVariable("nickName", sender.getName()));
            userVars.add(new SFSUserVariable("level", (int) 1));
            userVars.add(new SFSUserVariable("avatarState", "halted"));
            userVars.add(new SFSUserVariable("capturedBy", (int) 0));
            userVars.add(new SFSUserVariable("capturedMethod", (int) 0));
            userVars.add(new SFSUserVariable("inactive", false));
            userVars.add(new SFSUserVariable("hacks", (int) 0));
            userVars.add(new SFSUserVariable("pickingup", true));
            userVars.add(new SFSUserVariable("x", (double) 0));
            userVars.add(new SFSUserVariable("y", (double) 0));

            this.getApi().setUserVariables(sender, userVars);
            sender.setProperty(
                    "ExoPlayer",
                    new ExoPlayer(
                            sender,
                            (ExoSuit[])
                                    this.getParentExtension()
                                            .handleInternalMessage("getSuits", null)));

            roomSettings.setMaxUsers(8);
            roomSettings.setMaxVariablesAllowed(50);
            roomSettings.setDynamic(true);
            roomSettings.setAutoRemoveMode(SFSRoomRemoveMode.WHEN_EMPTY);
            roomSettings.setExtension(
                    new CreateRoomSettings.RoomExtensionSettings(
                            "Exonaut", "xyz.openexonaut.extension.room.ExonautRoomExtension"));
            roomSettings.setGame(true);

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

            this.getApi()
                    .quickJoinOrCreateRoom(
                            sender,
                            new MatchExpression("mode", StringMatch.EQUALS, mode)
                                    .and("state", StringMatch.NOT_EQUALS, "play"),
                            Arrays.asList("default"),
                            roomSettings);

            ISFSObject responseParams = new SFSObject();
            responseParams.putUtfString("roomName", sender.getLastJoinedRoom().getName());
            send("findRoom", responseParams, sender);
        } catch (Exception e) {
            trace(ExtensionLogLevel.ERROR, e);
        }
    }
}
