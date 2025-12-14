package xyz.openexonaut.extension.room.reqhandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

public class PingHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        trace(
                ExtensionLogLevel.DEBUG,
                String.format("room ping from %s (id %d)", sender.getName(), sender.getId()));

        send("pingAck", new SFSObject(), sender);
    }
}
