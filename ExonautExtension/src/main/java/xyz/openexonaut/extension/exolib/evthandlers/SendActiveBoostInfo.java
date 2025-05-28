package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendActiveBoostInfo {
    public static void handle(Room room, ExoPlayer player, ISFSObject params) {
        ISFSArray eventArray = new SFSArray();
        ExoItem[] items = (ExoItem[]) room.getExtension().handleInternalMessage("getItems", null);
        for (int i = 0; i < items.length; i++) {
            ExoItem item = items[i];
            item.tick(); // report accurate times
            if (!item.active()) {
                ISFSObject oppResponse = new SFSObject();
                oppResponse.putInt("playerId", params.getInt("playerId"));
                oppResponse.putInt("msgType", ExoEvtEnum.EVT_ACTIVE_BOOST_INFO.code);
                oppResponse.putInt("boostIdx", i);
                oppResponse.putFloat("boostTime", item.timeToRespawn());
                eventArray.addSFSObject(oppResponse);
            }
        }

        ISFSObject response = new SFSObject();
        response.putSFSArray("events", eventArray);
        ExoSendUtils.sendEventToOne(room, response, player.user);
    }
}
