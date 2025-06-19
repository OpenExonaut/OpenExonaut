package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.messages.*;

public class ErrorReceipt {
    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        room.getExtension()
                .handleInternalMessage(
                        "trace",
                        new ExoTraceArgs(
                                ExtensionLogLevel.WARN,
                                String.format(
                                        "unhandled event %d (%s) from sender %s (id %d)",
                                        params.getInt("msgType"),
                                        evtName,
                                        player.user.getName(),
                                        player.user.getId())));
    }
}
