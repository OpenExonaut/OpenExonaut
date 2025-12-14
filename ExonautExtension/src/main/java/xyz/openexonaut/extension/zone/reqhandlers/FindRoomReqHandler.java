package xyz.openexonaut.extension.zone.reqhandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.utils.*;

public class FindRoomReqHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        String mode = ExoParamUtils.deserializeField(params, "mode", String.class);

        if (mode != null) {
            ExoEntryUtils.findRoom(sender, mode, getParentExtension().getParentZone());
        }
    }
}
