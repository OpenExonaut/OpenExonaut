package xyz.openexonaut.extension.room.reqhandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

public class GefHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        trace(
                ExtensionLogLevel.DEBUG,
                String.format("room gef from %s (id %d)", sender.getName(), sender.getId()));

        // do nothing. this is a debug request intended to instantly end the match
    }
}
