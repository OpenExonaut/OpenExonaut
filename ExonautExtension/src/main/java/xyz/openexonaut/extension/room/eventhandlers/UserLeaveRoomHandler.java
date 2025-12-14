package xyz.openexonaut.extension.room.eventhandlers;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.extensions.*;

public class UserLeaveRoomHandler extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) {
        User user = (User) event.getParameter(SFSEventParam.USER);

        trace(
                ExtensionLogLevel.DEBUG,
                String.format("room leave from %s (id %d)", user.getName(), user.getId()));

        getParentExtension().handleInternalMessage("removePlayer", user);
    }
}
