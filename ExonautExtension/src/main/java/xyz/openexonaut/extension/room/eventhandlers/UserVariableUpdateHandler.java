package xyz.openexonaut.extension.room.eventhandlers;

import java.util.*;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.game.*;

public class UserVariableUpdateHandler extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) {
        User user = (User) event.getParameter(SFSEventParam.USER);
        ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");

        if (player != null) {
            @SuppressWarnings("rawtypes")
            List changedVariables = (List) event.getParameter(SFSEventParam.VARIABLES);
            player.updateVariables(changedVariables, getApi());
        } else {
            trace(
                    ExtensionLogLevel.WARN,
                    "null player for user " + user.getId() + " \"" + user.getName() + "\"");
        }
    }
}
