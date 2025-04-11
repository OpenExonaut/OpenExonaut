package xyz.openexonaut.extension.exolib.reqhandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.*;

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
            EvtEnum eventType = EvtEnum.getFromCode(params.getInt("msgType"));
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
                eventType.handle(this, player, params);
            }
        }
    }

    public void respondToOne(ISFSObject response, User recipient) {
        this.send("sendEvents", response, recipient);
    }

    public void respondToAll(ISFSObject response) {
        this.send(
                "sendEvents", response, this.getParentExtension().getParentRoom().getPlayersList());
    }

    public void traceIt(ExtensionLogLevel level, String what) {
        trace(level, what);
    }
}
