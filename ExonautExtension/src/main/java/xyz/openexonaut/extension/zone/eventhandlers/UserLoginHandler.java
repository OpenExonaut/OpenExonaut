package xyz.openexonaut.extension.zone.eventhandlers;

import com.smartfoxserver.bitswarm.sessions.*;
import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.exceptions.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.utils.*;

public class UserLoginHandler extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException {
        Session session = (Session) event.getParameter(SFSEventParam.SESSION);
        String username = (String) event.getParameter(SFSEventParam.LOGIN_NAME);
        String password = (String) event.getParameter(SFSEventParam.LOGIN_PASSWORD);

        trace(
                ExtensionLogLevel.DEBUG,
                String.format(
                        "zone login with username %s (session id %d)", username, session.getId()));

        if (!ExoEntryUtils.login(
                session, username, password, getParentExtension().getParentZone())) {
            throw new SFSLoginException("User matching provided credentials not found.");
        }
        // return without exception marks successful pass of authentication stage of login
    }
}
