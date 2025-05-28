package xyz.openexonaut.extension.exolib.evthandlers;

import java.util.concurrent.atomic.*;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendMyShotPosition {
    public static void handle(Room room, ExoPlayer player, ISFSObject params) {
        AtomicInteger nextBulletId =
                (AtomicInteger) room.getExtension().handleInternalMessage("getNextBulletId", null);

        float angle = params.getFloat("angle");
        float inc = params.getFloat("inc");
        float x = params.getFloat("x");
        float y = params.getFloat("y");
        int firstBnum = nextBulletId.get();

        ExoWeapon weapon =
                (ExoWeapon)
                        room.getExtension()
                                .handleInternalMessage("getWeapon", player.getWeaponId());
        float range = weapon.Range;
        float velocity = weapon.Velocity;
        float damage = weapon.Damage;
        int projectiles = weapon.Projectiles;

        ExoMod weaponMod = player.suit.WeaponMod;
        if (weaponMod.weapon.equals(weapon)) {
            range += weaponMod.Projectile_Range;
            damage += weaponMod.Damage_Per_Projectile;
            projectiles += weaponMod.Num_Projectiles;
        }

        float currentAngle = angle;
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
                                    x,
                                    y,
                                    player,
                                    (ExoProps)
                                            room.getExtension()
                                                    .handleInternalMessage("getProps", null)));
            currentAngle -= inc;
        }

        ISFSObject newShot = new SFSObject();
        newShot.putInt("playerId", params.getInt("playerId"));
        newShot.putInt("msgType", ExoEvtEnum.EVT_SEND_SHOT_POSITION.code);
        newShot.putFloat("angle", angle);
        newShot.putFloat("inc", inc);
        newShot.putFloat("x", x);
        newShot.putFloat("y", y);
        newShot.putInt("bnum", firstBnum);

        ISFSArray eventArray = new SFSArray();
        ISFSObject response = new SFSObject();
        eventArray.addSFSObject(newShot);
        response.putSFSArray("events", eventArray);
        ExoSendUtils.sendEventToAll(room, response);
    }
}
