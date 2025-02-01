package xyz.openexonaut.extension.room.eventhandlers;

import java.util.*;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.variables.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.*;

public class UserJoinRoomHandler extends BaseServerEventHandler {
    public static final int MIN_PLAYERS = 2;

    @Override
    public void handleServerEvent(ISFSEvent event) {
        User user = (User) event.getParameter(SFSEventParam.USER);
        ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");
        ExoPlayer[] players =
                (ExoPlayer[]) getParentExtension().handleInternalMessage("getPlayers", null);

        int i;
        for (i = 0; i < players.length; i++) {
            if (players[i] == null) {
                players[i] = player;
                player.id = i + 1;
                break;
            }
        }
        if (i == players.length) {
            trace(ExtensionLogLevel.WARN, "no room for new user");
        }

        if (getParentExtension().getParentRoom().getPlayersList().size() >= MIN_PLAYERS) {
            if (getParentExtension()
                    .getParentRoom()
                    .getVariable("state")
                    .getStringValue()
                    .equals("wait_for_min_players")) {
                List<RoomVariable> variableUpdate = new ArrayList<>();
                variableUpdate.add(new SFSRoomVariable("state", "countdown"));
                getApi().setRoomVariables(
                                null, getParentExtension().getParentRoom(), variableUpdate);

                getParentExtension().handleInternalMessage("startCountdown", null);
            }
        }
    }
}
