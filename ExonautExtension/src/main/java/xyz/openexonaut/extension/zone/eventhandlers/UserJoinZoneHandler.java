package xyz.openexonaut.extension.zone.eventhandlers;

import java.util.*;

import org.bson.*;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.variables.*;
import com.smartfoxserver.v2.exceptions.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.game.*;

public class UserJoinZoneHandler extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) throws SFSException {
        User user = (User) event.getParameter(SFSEventParam.USER);
        String tegid = (String) user.getSession().getProperty("tegid");
        String displayName = (String) user.getSession().getProperty("dname");

        user.setProperty("tegid", tegid);
        user.setProperty("ExoPlayer", new ExoPlayer(user));

        List<UserVariable> varUpdate = new ArrayList<>();

        if (tegid.equals("")) {
            displayName = user.getName();

            varUpdate.add(new SFSUserVariable("level", (int) 1));
        } else {
            Document playerObject =
                    (Document) getParentExtension().handleInternalMessage("getPlayerObject", tegid);

            varUpdate.add(new SFSUserVariable("level", playerObject.getInteger("Level")));
        }

        varUpdate.add(new SFSUserVariable("nickName", displayName));
        getApi().setUserVariables(user, varUpdate);
    }
}
