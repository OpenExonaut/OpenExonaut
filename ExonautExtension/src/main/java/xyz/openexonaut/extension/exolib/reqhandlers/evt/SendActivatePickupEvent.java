package xyz.openexonaut.extension.exolib.reqhandlers.evt;

import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.exolib.reqhandlers.*;

public class SendActivatePickupEvent {
    public static void handle(EvtHandler evtHandler, ExoPlayer player, ISFSObject params) {
        ExoItem[] items =
                (ExoItem[]) evtHandler.getParentExtension().handleInternalMessage("getItems", null);

        int pickupIdx = params.getInt("pIdx");
        int pickupType = params.getInt("pType");
        int effectTime = params.getInt("eTime");

        if (pickupType == ExoPickupEnum.boost_random.id) {
            pickupType = (int) (Math.random() * 4);
            params.putInt("pType", pickupType);
        }

        items[pickupIdx].grabbed();

        if (pickupType < 4) {
            player.setBoost(
                    pickupType, effectTime, evtHandler.getParentExtension().getParentRoom());
        } else if (pickupType > 9 && pickupType < 14) {
            // TODO: set pickup for whole team
            player.setTeamBoost(
                    pickupType, effectTime, evtHandler.getParentExtension().getParentRoom());
        }

        Echo.handle(evtHandler, player, params);
    }
}
