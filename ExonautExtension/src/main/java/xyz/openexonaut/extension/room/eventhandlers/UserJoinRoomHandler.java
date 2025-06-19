package xyz.openexonaut.extension.room.eventhandlers;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.extensions.*;

public class UserJoinRoomHandler extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) {
        User user = (User) event.getParameter(SFSEventParam.USER);

        trace(
                ExtensionLogLevel.DEBUG,
                String.format("room join from %s (id %d)", user.getName(), user.getId()));

        getParentExtension().handleInternalMessage("spawnPlayer", user.getPlayerId());
    }
}
