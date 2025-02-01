package xyz.openexonaut.extension.room.eventhandlers;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.*;

public class UserLeaveRoomHandler extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) {
        User user = (User) event.getParameter(SFSEventParam.USER);
        ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");
        ExoPlayer[] players =
                (ExoPlayer[]) getParentExtension().handleInternalMessage("getPlayers", null);

        if (player != null) {
            user.removeProperty("ExoPlayer");
            for (int i = 0; i < players.length; i++) {
                if (players[i] == player) {
                    players[i] = null;
                    break;
                }
            }
        } else {
            trace(
                    ExtensionLogLevel.WARN,
                    "null player for user " + user.getId() + " \"" + user.getName() + "\"");
        }
    }
}
