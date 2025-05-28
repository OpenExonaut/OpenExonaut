package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.game.*;

public class SendActivatePickupEvent {
    private static final int randomRange =
            ExoPickupEnum.boost_speed.id - ExoPickupEnum.boost_armor.id + 1;

    public static void handle(Room room, ExoPlayer player, ISFSObject params) {
        ExoItem[] items = (ExoItem[]) room.getExtension().handleInternalMessage("getItems", null);

        int pickupIdx = params.getInt("pIdx");
        int pickupType = params.getInt("pType");
        int effectTime = params.getInt("eTime");

        if (pickupType == ExoPickupEnum.boost_random.id) {
            pickupType = (int) (Math.random() * randomRange) + ExoPickupEnum.boost_armor.id;
            params.putInt("pType", pickupType);
        }

        items[pickupIdx].grabbed();

        if (pickupType >= ExoPickupEnum.boost_armor.id
                && pickupType <= ExoPickupEnum.boost_speed.id) {
            player.setBoost(pickupType, effectTime, room);
        } else if (pickupType >= ExoPickupEnum.boost_team_armor.id
                && pickupType <= ExoPickupEnum.boost_team_speed.id) {
            // TODO: set pickup for whole team
            player.setTeamBoost(pickupType, effectTime, room);
        }

        Echo.handle(room, player, params);
    }
}
