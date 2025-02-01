package xyz.openexonaut.extension.room.reqhandlers.evt;

import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.room.reqhandlers.*;

public class Echo {
    public static void handle(EvtHandler evtHandler, ExoPlayer player, ISFSObject params) {
        ISFSArray eventArray = new SFSArray();
        ISFSObject response = new SFSObject();
        eventArray.addSFSObject(params);
        response.putSFSArray("events", eventArray);
        evtHandler.respondToAll(response);
    }
}
