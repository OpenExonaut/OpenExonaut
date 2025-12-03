package xyz.openexonaut.extension.room.reqhandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.game.*;

public class EvtHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        try {
            trace(
                    ExtensionLogLevel.DEBUG,
                    String.format("room evt from %s (id %d)", sender.getName(), sender.getId()));

            ExoEvtEnum.handleEvtReq(
                    getParentExtension().getParentRoom(),
                    (ExoPlayer) sender.getProperty("ExoPlayer"),
                    params);
        } catch (ExoRuntimeException e) {
            getLogger().warn("room evt sanitization exception", e);
        } catch (Exception e) {
            getLogger().error("room evt error", e);
        }
    }
}
