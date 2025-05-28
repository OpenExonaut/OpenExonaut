package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendOpponentInfo {
    public static void handle(Room room, ExoPlayer player, ISFSObject params) {
        ISFSArray eventArray = new SFSArray();
        for (User user : room.getPlayersList()) {
            if (user != null) {
                if (!user.equals(player.user)) {
                    ExoPlayer opp = (ExoPlayer) user.getProperty("ExoPlayer");
                    if (opp != null) {
                        ISFSObject oppResponse = new SFSObject();
                        oppResponse.putInt("playerId", params.getInt("playerId"));
                        oppResponse.putInt("msgType", ExoEvtEnum.EVT_SEND_OPP_INFO.code);
                        oppResponse.putInt("oppId", user.getPlayerId());
                        oppResponse.putInt("currHealth", (int) opp.getHealth());
                        oppResponse.putInt("boost", opp.getBoostResponse());
                        oppResponse.putInt("captures", opp.getHacks());
                        oppResponse.putInt("weaponIdx", opp.getWeaponId());
                        eventArray.addSFSObject(oppResponse);
                    }
                }
            }
        }

        ISFSObject response = new SFSObject();
        response.putSFSArray("events", eventArray);
        ExoSendUtils.sendEventToOne(room, response, player.user);
    }
}
