package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendRocketExplode {
    public static final int msgType = 16;
    public final float x;
    public final float y;
    public final int num;

    public SendRocketExplode(Float x, Float y, Integer num) {
        this.x = x;
        this.y = y;
        this.num = num;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        SendRocketExplode args = ExoParamUtils.deserialize(params, SendRocketExplode.class);

        if (args == null) {
            ErrorReceipt.handle(room, player, params, evtName);
        } else {
            // TODO: explosives

            ExoSendUtils.sendEventObjectToAll(
                    room, ExoParamUtils.serialize(args, player.user.getPlayerId()));
        }
    }
}
