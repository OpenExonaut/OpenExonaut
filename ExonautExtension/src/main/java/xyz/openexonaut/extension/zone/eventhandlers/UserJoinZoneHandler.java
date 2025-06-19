package xyz.openexonaut.extension.zone.eventhandlers;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.utils.*;

public class UserJoinZoneHandler extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) {
        User user = (User) event.getParameter(SFSEventParam.USER);

        ExoEntryUtils.initUser(user, getParentExtension().getParentZone());
    }
}
