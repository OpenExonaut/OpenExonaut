package xyz.openexonaut.extension.zone.eventhandlers;

import com.smartfoxserver.bitswarm.sessions.*;
import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.exceptions.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.zone.messages.*;

public class UserLoginHandler extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException {
        String username = (String) event.getParameter(SFSEventParam.LOGIN_NAME);
        String password = (String) event.getParameter(SFSEventParam.LOGIN_PASSWORD);
        Session session = (Session) event.getParameter(SFSEventParam.SESSION);

        if (username.equals("")) {
            if (!password.equals("")) {
                trace(
                        ExtensionLogLevel.WARN,
                        "Unexpected: empty username (guest login) with non-empty password.");
            }
            session.setProperty("dname", "");
            session.setProperty("tegid", "");
        } else {
            String displayName =
                    (String)
                            getParentExtension()
                                    .handleInternalMessage(
                                            "checkLogin",
                                            new ExoLoginParameters(session, username, password));
            if (displayName == null) {
                throw new SFSLoginException("User matching provided credentials not found.");
            }
            session.setProperty("dname", displayName);
            session.setProperty("tegid", username);
        }
        // successful return marks successful pass of authentication stage of login
    }
}
