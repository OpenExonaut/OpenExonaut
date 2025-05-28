package xyz.openexonaut.extension.room.reqhandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

public class PingHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        trace(ExtensionLogLevel.DEBUG, "room ping from " + sender.toString());

        send("pingAck", new SFSObject(), sender);
    }
}
