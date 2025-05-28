package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.messages.*;

public class ErrorReceipt {
    public static void handle(Room room, ExoPlayer player, ISFSObject params) {
        ExoEvtEnum event = ExoEvtEnum.getFromCode(params.getInt("msgType"));
        String name = event != null ? event.toString() : "UNKNOWN";
        room.getExtension()
                .handleInternalMessage(
                        "trace",
                        new ExoTraceArgs(
                                ExtensionLogLevel.WARN,
                                "unhandled event "
                                        + params.getInt("msgType")
                                        + " ("
                                        + name
                                        + ") from playerId "
                                        + params.getInt("playerId")
                                        + ", sender "
                                        + player.user.getName()
                                        + " (id "
                                        + player.user.getId()
                                        + ")"));
    }
}
