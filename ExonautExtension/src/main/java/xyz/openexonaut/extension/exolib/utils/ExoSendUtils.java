package xyz.openexonaut.extension.exolib.utils;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

public final class ExoSendUtils {
    private ExoSendUtils() {}

    public static void sendEventArrayToOne(Room room, ISFSArray eventArray, User recipient) {
        ISFSObject response = new SFSObject();
        response.putSFSArray("events", eventArray);
        room.getExtension().send("sendEvents", response, recipient);
    }

    public static void sendEventArrayToAll(Room room, ISFSArray eventArray) {
        ISFSObject response = new SFSObject();
        response.putSFSArray("events", eventArray);
        room.getExtension().send("sendEvents", response, room.getPlayersList());
    }

    public static void sendEventObjectToOne(Room room, ISFSObject eventObject, User recipient) {
        ISFSArray eventArray = new SFSArray();
        eventArray.addSFSObject(eventObject);
        sendEventArrayToOne(room, eventArray, recipient);
    }

    public static void sendEventObjectToAll(Room room, ISFSObject eventObject) {
        ISFSArray eventArray = new SFSArray();
        eventArray.addSFSObject(eventObject);
        sendEventArrayToAll(room, eventArray);
    }
}
