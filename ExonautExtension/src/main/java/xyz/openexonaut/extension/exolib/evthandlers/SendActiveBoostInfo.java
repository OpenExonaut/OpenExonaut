package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendActiveBoostInfo {
    public static final int msgType = 25;
    public final int boostIdx;
    public final float boostTime;

    public SendActiveBoostInfo(Integer boostIdx, Float boostTime) {
        this.boostIdx = boostIdx;
        this.boostTime = boostTime;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        ISFSArray responseArray = new SFSArray();
        ISFSArray tickArray = new SFSArray();
        ExoItem[] items = (ExoItem[]) room.getExtension().handleInternalMessage("getItems", null);
        for (int i = 0; i < items.length; i++) {
            ExoItem item = items[i];
            item.tick(tickArray, room); // report accurate times
            if (!item.active()) {
                responseArray.addSFSObject(
                        ExoParamUtils.serialize(
                                new SendActiveBoostInfo(i, item.timeToRespawn()),
                                player.user.getPlayerId(room)));
            }
        }

        ExoSendUtils.sendEventArrayToOne(room, responseArray, player.user);
        if (tickArray.size() > 0) {
            ExoSendUtils.sendEventArrayToAll(room, tickArray);
        }
    }
}
