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
        SendGrenadeExplode args = ExoParamUtils.deserialize(params, SendGrenadeExplode.class);
        // this is the lone case where the playerId is not that of the sender; it is of the thrower
        // it looks like the grenade explosion event is sent by every player
        Integer playerId = ExoParamUtils.deserializeField(params, "playerId", Integer.class);

        if (args == null || playerId == null) {
            ErrorReceipt.handle(room, player, params, evtName);
        } else {
            room.getExtension().handleInternalMessage("explodeGrenade", args);

            ExoSendUtils.sendEventObjectToAll(room, ExoParamUtils.serialize(args, playerId));
        }
    }
}
