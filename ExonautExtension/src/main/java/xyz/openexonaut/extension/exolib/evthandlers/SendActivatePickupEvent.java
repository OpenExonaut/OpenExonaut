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
                        ? ExoPickupEnum.getRandomIndividualPickup()
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

            items[args.pIdx].grabbed(tickArray, room);

            if (ExoPickupEnum.isIndividualBoost(args.pType)) {
                player.setBoost(args.pType, args.eTime, tickArray, room);
            } else if (ExoPickupEnum.isTeamBoost(args.pType)) {
                String faction = player.user.getVariable("faction").getStringValue();
                for (User user : room.getPlayersList()) {
                    if (user.getVariable("faction").getStringValue().equals(faction)) {
                        ((ExoPlayer) user.getProperty("ExoPlayer"))
                                .setTeamBoost(args.pType, args.eTime, tickArray, room);
                    }
                }
            }

            if (tickArray.size() > 0) {
                ExoSendUtils.sendEventArrayToAll(room, tickArray);
            }

            ExoSendUtils.sendEventObjectToAll(
                    room, ExoParamUtils.serialize(args, player.user.getPlayerId(room)));
        }
    }
}
