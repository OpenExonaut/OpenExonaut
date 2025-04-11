package xyz.openexonaut.extension.exolib.reqhandlers.evt;

import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.exolib.reqhandlers.*;

public class SendOpponentInfo {
    public static void handle(EvtHandler evtHandler, ExoPlayer player, ISFSObject params) {
        // TODO: is this really how this works?
        ISFSArray eventArray = new SFSArray();
        ExoPlayer[] players =
                (ExoPlayer[])
                        evtHandler.getParentExtension().handleInternalMessage("getPlayers", null);
        for (int i = 0; i < players.length; i++) {
            ExoPlayer opp = players[i];
            if (opp != null) {
                if (!opp.user.equals(player.user)) {
                    ISFSObject oppResponse = new SFSObject();
                    oppResponse.putInt("playerId", params.getInt("playerId"));
                    oppResponse.putInt("msgType", EvtEnum.EVT_SEND_OPP_INFO.code);
                    oppResponse.putInt("oppId", opp.id); // TODO: this id?
                    synchronized (opp) {
                        // TODO: track health
                        oppResponse.putInt("currHealth", (int) opp.health);
                        // TODO: track boosts
                        oppResponse.putInt("boost", opp.boost);
                        // TODO: track captures
                        oppResponse.putInt("captures", opp.hacks);
                        oppResponse.putInt("weaponIdx", opp.weaponId);
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
