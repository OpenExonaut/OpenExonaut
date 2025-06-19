package xyz.openexonaut.extension.exolib.evthandlers;

import java.util.concurrent.atomic.*;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendMyGrenadePosition {
    public static final int msgType = 5;
    public final float angle;
    public final int type;
    public final float x;
    public final float y;
    public int num = -1;

    public SendMyGrenadePosition(Float angle, Integer type, Float x, Float y) {
        this.angle = angle;
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        SendMyGrenadePosition args = ExoParamUtils.deserialize(params, SendMyGrenadePosition.class);

        if (args == null) {
            ErrorReceipt.handle(room, player, params, evtName);
        } else {
            // is there a reason why the client sends its own number of thrown grenades in the input
            // "num" field?
            args.num =
                    ((AtomicInteger)
                                    room.getExtension()
                                            .handleInternalMessage("getNextGrenadeId", null))
                            .getAndIncrement();

            ExoSendUtils.sendEventObjectToAll(
                    room, ExoParamUtils.serialize(args, player.user.getPlayerId()));
        }
    }
}
