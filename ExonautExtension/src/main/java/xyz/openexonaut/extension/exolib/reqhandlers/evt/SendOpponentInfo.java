package xyz.openexonaut.extension.exolib.reqhandlers.evt;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.exolib.reqhandlers.*;

public class SendOpponentInfo {
    public static void handle(EvtHandler evtHandler, ExoPlayer player, ISFSObject params) {
        ISFSArray eventArray = new SFSArray();
        for (User user : evtHandler.getParentExtension().getParentRoom().getPlayersList()) {
            if (!user.equals(player.user)) {
                ExoPlayer opp = (ExoPlayer) user.getProperty("ExoPlayer");
                if (opp != null) {
                    ISFSObject oppResponse = new SFSObject();
                    oppResponse.putInt("playerId", params.getInt("playerId"));
                    oppResponse.putInt("msgType", EvtEnum.EVT_SEND_OPP_INFO.code);
                    oppResponse.putInt("oppId", user.getPlayerId());
                    synchronized (opp) {
                        oppResponse.putInt("currHealth", (int) opp.getHealth());
                        oppResponse.putInt("boost", opp.getBoostResponse());
                        oppResponse.putInt("captures", opp.getHacks());
                        oppResponse.putInt("weaponIdx", opp.getWeaponId());
                    }
                    eventArray.addSFSObject(oppResponse);
                }
            }
        }

        ISFSObject response = new SFSObject();
        response.putSFSArray("events", eventArray);
        evtHandler.respondToOne(response, player.user);
    }
}
