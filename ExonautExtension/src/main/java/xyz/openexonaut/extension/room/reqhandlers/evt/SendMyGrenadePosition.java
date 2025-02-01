package xyz.openexonaut.extension.room.reqhandlers.evt;

import java.util.concurrent.atomic.*;

import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.room.reqhandlers.*;

public class SendMyGrenadePosition {
    public static void handle (EvtHandler evtHandler, ExoPlayer player, ISFSObject params) {
        ISFSObject newGrenade = new SFSObject();
        newGrenade.putInt("playerId", params.getInt("playerId"));
        newGrenade.putInt("msgType", 5);
        newGrenade.putFloat("angle", params.getFloat("angle"));
        newGrenade.putInt("type", params.getInt("type"));
        newGrenade.putFloat("x", params.getFloat("x"));
        newGrenade.putFloat("y", params.getFloat("y"));

        // is there a reason why the client sends its own number of thrown grenades in the input "num" field?
        newGrenade.putInt("num", ((AtomicInteger)evtHandler.getParentExtension().handleInternalMessage("getNextGrenadeId", null)).getAndIncrement());

        ISFSArray eventArray = new SFSArray();
        ISFSObject response = new SFSObject();
        eventArray.addSFSObject(newGrenade);
        response.putSFSArray("events", eventArray);
        evtHandler.respondToAll(response);
    }
}
