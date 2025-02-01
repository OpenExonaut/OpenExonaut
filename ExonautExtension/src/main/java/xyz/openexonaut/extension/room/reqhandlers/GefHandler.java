package xyz.openexonaut.extension.room.reqhandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

public class GefHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        trace(ExtensionLogLevel.DEBUG, "room gef from " + sender.toString());

        this.getApi().disconnectUser(sender); // TODO: how should this actually work?
    }
}
