package xyz.openexonaut.extension.room.eventhandlers;

import java.util.*;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.variables.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.data.*;

public class UserJoinRoomHandler extends BaseServerEventHandler {
    @Override
    public void handleServerEvent(ISFSEvent event) {
        User user = (User) event.getParameter(SFSEventParam.USER);

        getParentExtension().handleInternalMessage("spawnPlayer", user.getPlayerId());

        if (getParentExtension().getParentRoom().getPlayersList().size()
                >= ((ExoProps) getParentExtension().handleInternalMessage("getProps", null))
                        .minPlayers) {
            if (getParentExtension()
                    .getParentRoom()
                    .getVariable("state")
                    .getStringValue()
                    .equals("wait_for_min_players")) {
                List<RoomVariable> variableUpdate = new ArrayList<>();
                variableUpdate.add(new SFSRoomVariable("state", "countdown"));
                getApi().setRoomVariables(
                                null, getParentExtension().getParentRoom(), variableUpdate);

                getParentExtension().handleInternalMessage("startCountdown", null);
            }
        }
    }
}
