package xyz.openexonaut.extension.room.eventhandlers;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.game.*;

public class UserDisconnectRoomHandler extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) {
        try {
            User user = (User) event.getParameter(SFSEventParam.USER);

            trace(
                    ExtensionLogLevel.DEBUG,
                    String.format("room disconnect from %s (id %d)", user.getName(), user.getId()));

            getParentExtension().handleInternalMessage("removePlayer", user);
        } catch (ExoRuntimeException e) {
            getLogger().warn("room user disconnect sanitization exception", e);
        } catch (Exception e) {
            getLogger().error("room user disconnect error", e);
        }
    }
}
