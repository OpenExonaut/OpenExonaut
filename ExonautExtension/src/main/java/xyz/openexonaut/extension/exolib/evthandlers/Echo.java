package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class Echo {
    public static void handle(Room room, ExoPlayer player, ISFSObject params) {
        ISFSArray eventArray = new SFSArray();
        ISFSObject response = new SFSObject();
        eventArray.addSFSObject(params);
        response.putSFSArray("events", eventArray);
        ExoSendUtils.sendEventToAll(room, response);
    }
}
