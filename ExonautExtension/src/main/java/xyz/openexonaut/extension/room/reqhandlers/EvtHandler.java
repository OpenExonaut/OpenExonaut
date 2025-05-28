package xyz.openexonaut.extension.room.reqhandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.game.*;

public class EvtHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        trace(ExtensionLogLevel.DEBUG, "room evt from " + sender.toString());
        ExoPlayer player = (ExoPlayer) sender.getProperty("ExoPlayer");
        if (player == null) {
            trace(
                    ExtensionLogLevel.WARN,
                    "null player for user " + sender.getId() + " \"" + sender.getName() + "\"");
        }

        if (params.containsKey("msgType")) {
            ExoEvtEnum eventType = ExoEvtEnum.getFromCode(params.getInt("msgType"));
            if (eventType == null) {
                trace(
                        ExtensionLogLevel.WARN,
                        "unknown event "
                                + params.getInt("msgType")
                                + " from playerId "
                                + params.getInt("playerId")
                                + ", sender "
                                + player.user.getName()
                                + " (id "
                                + player.user.getId()
                                + ")");
            } else {
                eventType.handle(getParentExtension().getParentRoom(), player, params);
            }
        }
    }
}
