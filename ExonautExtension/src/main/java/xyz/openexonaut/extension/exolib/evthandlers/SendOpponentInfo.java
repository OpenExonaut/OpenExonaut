package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendOpponentInfo {
    public static final int msgType = 23;
    public final int oppId;
    public final int currHealth;
    public final int boost;
    public final int captures;
    public final int weaponIdx;

    public SendOpponentInfo(
            Integer oppId, Integer currHealth, Integer boost, Integer captures, Integer weaponIdx) {
        this.oppId = oppId;
        this.currHealth = currHealth;
        this.boost = boost;
        this.captures = captures;
        this.weaponIdx = weaponIdx;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        ISFSArray eventArray = new SFSArray();
        for (User user : room.getPlayersList()) {
            if (user != null) {
                if (!user.equals(player.user)) {
                    ExoPlayer opp = (ExoPlayer) user.getProperty("ExoPlayer");
                    if (opp != null) {
                        eventArray.addSFSObject(
                                ExoParamUtils.serialize(
                                        new SendOpponentInfo(
                                                user.getPlayerId(room),
                                                (int) opp.getHealth(),
                                                opp.getBoostResponse(),
                                                opp.getHacks(),
                                                opp.getWeaponId()),
                                        player.user.getPlayerId(room)));
                    }
                }
            }
        }

        ExoSendUtils.sendEventArrayToOne(room, eventArray, player.user);
    }
}
