package xyz.openexonaut.extension.zone.eventhandlers;

import java.util.*;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.extensions.*;

public class UserLogoutHandler extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) {
        User user = (User) event.getParameter(SFSEventParam.USER);
        @SuppressWarnings("rawtypes")
        List joinedRooms = (List) event.getParameter(SFSEventParam.JOINED_ROOMS);

        trace(
                ExtensionLogLevel.DEBUG,
                String.format("zone logout from %s (id %d)", user.getName(), user.getId()));

        for (Object roomObject : joinedRooms) {
            Room room = (Room) roomObject;
            room.getExtension().handleInternalMessage("removePlayer", user);
        }
    }
}
