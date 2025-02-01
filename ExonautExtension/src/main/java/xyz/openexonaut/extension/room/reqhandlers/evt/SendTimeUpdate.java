package xyz.openexonaut.extension.room.reqhandlers.evt;

import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.room.reqhandlers.*;

public class SendTimeUpdate {
    public static void handle(EvtHandler evtHandler, ExoPlayer player, ISFSObject params) {
        ISFSObject timeAck = new SFSObject();
        timeAck.putInt("playerId", params.getInt("playerId"));
        timeAck.putInt("msgType", 24);
        timeAck.putInt(
                "time",
                (Integer)
                                evtHandler
                                        .getParentExtension()
                                        .handleInternalMessage("getTimeLimit", null)
                        - evtHandler
                                .getParentExtension()
                                .getParentRoom()
                                .getVariable("time")
                                .getIntValue());

        ISFSArray eventArray = new SFSArray();
        ISFSObject response = new SFSObject();
        eventArray.addSFSObject(timeAck);
        response.putSFSArray("events", eventArray);
        evtHandler.respondToOne(response, player.user);
    }
}
