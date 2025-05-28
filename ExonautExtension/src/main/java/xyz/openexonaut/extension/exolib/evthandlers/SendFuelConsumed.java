package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;

public class SendFuelConsumed {
    public static void handle(Room room, ExoPlayer player, ISFSObject params) {
        int fuel = params.getInt("fuel");
        player.fuelConsumed += fuel;
    }
}
