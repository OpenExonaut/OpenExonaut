package xyz.openexonaut.extension.exolib.utils;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

public abstract class ExoSendUtils {
    public static void sendEventToOne(Room room, ISFSObject response, User recipient) {
        room.getExtension().send("sendEvents", response, recipient);
    }

    public static void sendEventToAll(Room room, ISFSObject response) {
        room.getExtension().send("sendEvents", response, room.getPlayersList());
    }
}
