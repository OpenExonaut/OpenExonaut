package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.game.*;

public class SendSniperLine {
    public static void handle(Room room, ExoPlayer player, ISFSObject params) {
        float startX = params.getFloat("xstart");
        float startY = params.getFloat("ystart");
        float endX = params.getFloat("xend");
        float endY = params.getFloat("yend");

        ExoWeapon weapon =
                (ExoWeapon)
                        room.getExtension()
                                .handleInternalMessage("getWeapon", player.getWeaponId());
        float damage = weapon.Damage;

        ExoMod weaponMod = player.suit.WeaponMod;
        if (weaponMod.weapon.equals(weapon)) {
            damage += weaponMod.Damage_Per_Projectile;
        }

        // the start point is the gun tip, the end point is where the client thinks they made a hit.
        // the client might not be aware of a change (and anyway doesn't tell what they hit), so we
        // need to investigate ourselves
        room.getExtension()
                .handleInternalMessage(
                        "handleSnipe",
                        new ExoBullet(
                                startX,
                                startY,
                                endX,
                                endY,
                                damage,
                                player,
                                (ExoProps)
                                        room.getExtension()
                                                .handleInternalMessage("getProps", null)));

        Echo.handle(room, player, params);
    }
}
