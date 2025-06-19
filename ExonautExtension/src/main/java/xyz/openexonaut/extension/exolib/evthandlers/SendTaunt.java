package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendTaunt {
    public static final int msgType = 17;
    public final int num;

    public SendTaunt(Integer num) {
        this.num = num;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        Integer num = ExoParamUtils.deserializeField(params, "num", Integer.class);
        if (num == null) {
            ErrorReceipt.handle(room, player, params, evtName);
        } else {
            ExoSendUtils.sendEventObjectToAll(
                    room, ExoParamUtils.serialize(new SendTaunt(num), player.user.getPlayerId()));
        }
    }
}
