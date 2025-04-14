package xyz.openexonaut.extension.exolib.reqhandlers.evt;

import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.exolib.reqhandlers.*;

public class ErrorReceipt {
    public static void handle(EvtHandler evtHandler, ExoPlayer player, ISFSObject params) {
        EvtEnum event = EvtEnum.getFromCode(params.getInt("msgType"));
        String name = event != null ? event.toString() : "UNKNOWN";
        evtHandler.traceIt(
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
                        + ")");
    }
}
