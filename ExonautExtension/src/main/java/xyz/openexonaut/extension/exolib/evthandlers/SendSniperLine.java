package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.resources.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendSniperLine {
    public static final int msgType = 18;
    public final float xstart;
    public final float ystart;
    public final float xend;
    public final float yend;

    public SendSniperLine(Float xstart, Float ystart, Float xend, Float yend) {
        this.xstart = xstart;
        this.ystart = ystart;
        this.xend = xend;
        this.yend = yend;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        SendSniperLine args = ExoParamUtils.deserialize(params, SendSniperLine.class);

        if (args == null) {
            ErrorReceipt.handle(room, player, params, evtName);
        } else {
            ExoWeapon weapon = ExoGameData.getWeapon(player.getWeaponId());
            float damage = weapon.Damage;

            ExoMod weaponMod = player.getSuit().WeaponMod;
            if (weaponMod.weapon.equals(weapon)) {
                damage += weaponMod.Damage_Per_Projectile;
            }

            // the start point is the gun tip, the end point is where the client thinks they hit.
            // the client might not be aware of a change (and anyway doesn't tell what they hit), so
            // we need to investigate ourselves
            room.getExtension()
                    .handleInternalMessage(
                            "handleSnipe",
                            new ExoBullet(
                                    args.xstart,
                                    args.ystart,
                                    args.xend,
                                    args.yend,
                                    damage,
                                    player));

            ExoSendUtils.sendEventObjectToAll(
                    room, ExoParamUtils.serialize(args, player.user.getPlayerId()));
        }
    }
}
