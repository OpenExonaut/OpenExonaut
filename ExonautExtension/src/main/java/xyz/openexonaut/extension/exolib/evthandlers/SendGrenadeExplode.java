package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendGrenadeExplode {
    public static final int msgType = 9;
    public final float x;
    public final float y;
    public final int num;

    public SendGrenadeExplode(Float x, Float y, Integer num) {
        this.x = x;
        this.y = y;
        this.num = num;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        // TODO: replace with grenade's thrower id. (lone instance of sender subversion)
        SendGrenadeExplode args = ExoParamUtils.deserialize(params, SendGrenadeExplode.class);
        Integer playerId = ExoParamUtils.deserializeField(params, "playerId", Integer.class);

        if (args == null || playerId == null) {
            ErrorReceipt.handle(room, player, params, evtName);
        } else {
            // TODO: explosives

            ExoSendUtils.sendEventObjectToAll(room, ExoParamUtils.serialize(args, playerId));
        }
    }
}
