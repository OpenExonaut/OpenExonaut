package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendChangeWeapon {
    public static final int msgType = 6;
    public final int idx;

    public SendChangeWeapon(Integer idx) {
        this.idx = idx;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        Integer idx = ExoParamUtils.deserializeField(params, "idx", Integer.class);
        if (idx == null) {
            ErrorReceipt.handle(room, player, params, evtName);
        } else {
            ExoSendUtils.sendEventObjectToAll(
                    room,
                    ExoParamUtils.serialize(new SendChangeWeapon(idx), player.user.getPlayerId()));
        }
    }
}
