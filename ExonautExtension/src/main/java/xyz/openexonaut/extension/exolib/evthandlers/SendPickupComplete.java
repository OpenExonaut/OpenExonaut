package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;

public class SendPickupComplete {
    public static final int msgType = 22;
    public final int uPickup;

    public SendPickupComplete(Integer uPickup) {
        this.uPickup = uPickup;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        ErrorReceipt.handle(room, player, params, evtName);
    }
}
