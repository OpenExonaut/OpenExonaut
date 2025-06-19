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
        @SuppressWarnings("rawtypes") // SFS2X gives a raw list
        List changedVariables = (List) event.getParameter(SFSEventParam.VARIABLES);

        trace(
                ExtensionLogLevel.DEBUG,
                String.format("variable update from %s (id %d)", user.getName(), user.getId()));

        ((ExoPlayer) (user).getProperty("ExoPlayer")).updateVariables(changedVariables);
    }
}
