package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendActivatePickupEvent {
    public static final int msgType = 8;
    public final int myIdx;
    public final int pType;
    public final int pIdx;
    public final int pTime;
    public final int eTime;

    public SendActivatePickupEvent(
            Integer myIdx, Integer pType, Integer pIdx, Integer pTime, Integer eTime) {
        this.myIdx = myIdx;
        this.pIdx = pIdx;
        this.pTime = pTime;
        this.eTime = eTime;

        this.pType =
                pType == ExoPickupEnum.boost_random.id
                        ? (int)
                                        (Math.random()
                                                * (ExoPickupEnum.boost_speed.id
                                                        - ExoPickupEnum.boost_armor.id
                                                        + 1))
                                + ExoPickupEnum.boost_armor.id
                        : pType;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        SendActivatePickupEvent args =
                ExoParamUtils.deserialize(params, SendActivatePickupEvent.class);

        if (args == null) {
            ErrorReceipt.handle(room, player, params, evtName);
        } else {
            ISFSArray tickArray = new SFSArray();
            ExoItem[] items =
                    (ExoItem[]) room.getExtension().handleInternalMessage("getItems", null);

            items[args.pIdx].grabbed(tickArray);

            if (args.pType >= ExoPickupEnum.boost_armor.id
                    && args.pType <= ExoPickupEnum.boost_speed.id) {
                player.setBoost(args.pType, args.eTime, tickArray);
            } else if (args.pType >= ExoPickupEnum.boost_team_armor.id
                    && args.pType <= ExoPickupEnum.boost_team_speed.id) {
                // TODO: set pickup for whole team
                player.setTeamBoost(args.pType, args.eTime, tickArray);
            }

            if (tickArray.size() > 0) {
                ExoSendUtils.sendEventArrayToAll(room, tickArray);
            }

            ExoSendUtils.sendEventObjectToAll(
                    room, ExoParamUtils.serialize(args, player.user.getPlayerId()));
        }
    }
}
