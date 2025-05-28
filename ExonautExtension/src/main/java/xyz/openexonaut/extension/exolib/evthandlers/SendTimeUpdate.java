package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendTimeUpdate {
    public static void handle(Room room, ExoPlayer player, ISFSObject params) {
        ISFSObject timeAck = new SFSObject();
        timeAck.putInt("playerId", params.getInt("playerId"));
        timeAck.putInt("msgType", ExoEvtEnum.EVT_TIME_UPDATE.code);
        timeAck.putInt(
                "time",
                (Integer) room.getExtension().handleInternalMessage("getTimeLimit", null)
                        - room.getVariable("time").getIntValue());

        ISFSArray eventArray = new SFSArray();
        ISFSObject response = new SFSObject();
        eventArray.addSFSObject(timeAck);
        response.putSFSArray("events", eventArray);
        ExoSendUtils.sendEventToOne(room, response, player.user);
    }
}
