package xyz.openexonaut.extension.room.reqhandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.game.*;

public class PingHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        try {
            trace(
                    ExtensionLogLevel.DEBUG,
                    String.format("room ping from %s (id %d)", sender.getName(), sender.getId()));

            send("pingAck", new SFSObject(), sender);
        } catch (ExoRuntimeException e) {
            getLogger().warn("room ping sanitization exception", e);
        } catch (Exception e) {
            getLogger().error("room ping error", e);
        }
    }
}
