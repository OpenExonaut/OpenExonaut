package xyz.openexonaut.extension.zone.eventhandlers;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.utils.*;

public class UserJoinZoneHandler extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) {
        User user = (User) event.getParameter(SFSEventParam.USER);

        trace(
                ExtensionLogLevel.DEBUG,
                String.format("zone join from %s (id %d)", user.getName(), user.getId()));

        ExoEntryUtils.initUser(user, getParentExtension().getParentZone());
    }
}
