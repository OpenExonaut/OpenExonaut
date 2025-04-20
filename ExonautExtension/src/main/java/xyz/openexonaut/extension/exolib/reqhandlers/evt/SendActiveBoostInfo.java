package xyz.openexonaut.extension.exolib.reqhandlers.evt;

import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.exolib.reqhandlers.*;

public class SendActiveBoostInfo {
    public static void handle(EvtHandler evtHandler, ExoPlayer player, ISFSObject params) {
        ISFSArray eventArray = new SFSArray();
        ExoItem[] items =
                (ExoItem[]) evtHandler.getParentExtension().handleInternalMessage("getItems", null);
        for (int i = 0; i < items.length; i++) {
            ExoItem item = items[i];
            item.tick(); // report accurate times
            if (!item.active()) {
                ISFSObject oppResponse = new SFSObject();
                oppResponse.putInt("playerId", params.getInt("playerId"));
                oppResponse.putInt("msgType", EvtEnum.EVT_ACTIVE_BOOST_INFO.code);
                oppResponse.putInt("boostIdx", i);
                oppResponse.putFloat("boostTime", item.timeToRespawn());
                eventArray.addSFSObject(oppResponse);
            }
        }

        ISFSObject response = new SFSObject();
        response.putSFSArray("events", eventArray);
        evtHandler.respondToOne(response, player.user);
    }
}
