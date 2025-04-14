package xyz.openexonaut.extension.exolib.reqhandlers.evt;

import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.exolib.reqhandlers.*;

public class SendFuelConsumed {
    public static void handle(EvtHandler evtHandler, ExoPlayer player, ISFSObject params) {
        int fuel = params.getInt("fuel");
        player.fuelConsumed += fuel;
    }
}
