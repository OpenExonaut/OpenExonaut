package xyz.openexonaut.extension.exolib.evthandlers;

import java.util.concurrent.atomic.*;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendMyGrenadePosition {
    public static void handle(Room room, ExoPlayer player, ISFSObject params) {
        ISFSObject newGrenade = new SFSObject();
        newGrenade.putInt("playerId", params.getInt("playerId"));
        newGrenade.putInt("msgType", ExoEvtEnum.EVT_SEND_GRENADE_POSITION.code);
        newGrenade.putFloat("angle", params.getFloat("angle"));
        newGrenade.putInt("type", params.getInt("type"));
        newGrenade.putFloat("x", params.getFloat("x"));
        newGrenade.putFloat("y", params.getFloat("y"));

        // is there a reason why the client sends its own number of thrown grenades in the input
        // "num" field?
        newGrenade.putInt(
                "num",
                ((AtomicInteger)
                                room.getExtension().handleInternalMessage("getNextGrenadeId", null))
                        .getAndIncrement());

        ISFSArray eventArray = new SFSArray();
        ISFSObject response = new SFSObject();
        eventArray.addSFSObject(newGrenade);
        response.putSFSArray("events", eventArray);
        ExoSendUtils.sendEventToAll(room, response);
    }
}
