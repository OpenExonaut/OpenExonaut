package xyz.openexonaut.extension.room.reqhandlers.evt;

import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.room.reqhandlers.*;

public class SendSniperLine {
    public static void handle(EvtHandler evtHandler, ExoPlayer player, ISFSObject params) {
        float startX = params.getFloat("xstart");
        float startY = params.getFloat("ystart");
        float endX = params.getFloat("xend");
        float endY = params.getFloat("yend");

        ExoWeapon weapon =
                (ExoWeapon)
                        evtHandler
                                .getParentExtension()
                                .handleInternalMessage("getWeapon", player.weaponId);
        int damage = weapon.Damage;

        ExoMod weaponMod =
                ((ExoSuit)
                                evtHandler
                                        .getParentExtension()
                                        .handleInternalMessage("getSuit", player.suitId))
                        .WeaponMod;
        if (weaponMod.weapon.equals(weapon)) {
            damage += weaponMod.Damage_Per_Projectile;
        }

        // the start point is the gun tip, the end point is where the client thinks they made a hit.
        // the client might not be aware of a change (and anyway doesn't tell what they hit), so we
        // need to investigate ourselves
        evtHandler
                .getParentExtension()
                .handleInternalMessage(
                        "handleSnipe", new ExoBullet(startX, startY, endX, endY, damage, player));

        Echo.handle(evtHandler, player, params);
    }
}
