package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendTimeUpdate {
    public static final int msgType = 24;
    public final int time;

    public SendTimeUpdate(Integer time) {
        this.time = time;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        ExoSendUtils.sendEventObjectToOne(
                room,
                ExoParamUtils.serialize(
                        new SendTimeUpdate(
                                (Integer)
                                                room.getExtension()
                                                        .handleInternalMessage("getTimeLimit", null)
                                        - room.getVariable("time").getIntValue()),
                        player.user.getPlayerId()),
                player.user);
    }
}
