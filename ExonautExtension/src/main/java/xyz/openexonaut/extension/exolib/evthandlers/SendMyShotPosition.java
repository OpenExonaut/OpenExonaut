package xyz.openexonaut.extension.exolib.evthandlers;

import java.util.concurrent.atomic.*;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendMyShotPosition {
    public static final int msgType = 4;
    public final float angle;
    public final float inc;
    public final float x;
    public final float y;
    public int bnum = -1;

    public SendMyShotPosition(Float angle, Float inc, Float x, Float y) {
        this.angle = angle;
        this.inc = inc;
        this.x = x;
        this.y = y;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        SendMyShotPosition args = ExoParamUtils.deserialize(params, SendMyShotPosition.class);

        if (args == null) {
            ErrorReceipt.handle(room, player, params, evtName);
        } else {
            AtomicInteger nextBulletId =
                    (AtomicInteger)
                            room.getExtension().handleInternalMessage("getNextBulletId", null);

            args.bnum = nextBulletId.get();

            ExoWeapon weapon =
                    (ExoWeapon)
                            room.getExtension()
                                    .handleInternalMessage("getWeapon", player.getWeaponId());
            float range = weapon.Range;
            float velocity = weapon.Velocity;
            float damage = weapon.Damage;
            int projectiles = weapon.Projectiles;

            ExoMod weaponMod = player.getSuit().WeaponMod;
            if (weaponMod.weapon.equals(weapon)) {
                range += weaponMod.Projectile_Range;
                damage += weaponMod.Damage_Per_Projectile;
                projectiles += weaponMod.Num_Projectiles;
            }

            float currentAngle = args.angle;
            for (int i = 0; i < projectiles; i++) {
                room.getExtension()
                        .handleInternalMessage(
                                "spawnBullet",
                                new ExoBullet(
                                        nextBulletId.getAndIncrement(),
                                        range,
                                        velocity,
                                        currentAngle,
                                        damage,
                                        args.x,
                                        args.y,
                                        player,
                                        (ExoProps)
                                                room.getExtension()
                                                        .handleInternalMessage("getProps", null)));
                currentAngle -= args.inc;
            }

            ExoSendUtils.sendEventObjectToAll(
                    room, ExoParamUtils.serialize(args, player.user.getPlayerId()));
        }
    }
}
