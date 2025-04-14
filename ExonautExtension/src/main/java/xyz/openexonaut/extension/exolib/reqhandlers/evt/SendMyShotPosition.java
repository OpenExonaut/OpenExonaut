package xyz.openexonaut.extension.exolib.reqhandlers.evt;

import java.util.concurrent.atomic.*;

import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.exolib.reqhandlers.*;

public class SendMyShotPosition {
    public static void handle(EvtHandler evtHandler, ExoPlayer player, ISFSObject params) {
        AtomicInteger nextBulletId =
                (AtomicInteger)
                        evtHandler
                                .getParentExtension()
                                .handleInternalMessage("getNextBulletId", null);

        float angle = params.getFloat("angle");
        float inc = params.getFloat("inc");
        float x = params.getFloat("x");
        float y = params.getFloat("y");
        int firstBnum = nextBulletId.get();

        ExoWeapon weapon =
                (ExoWeapon)
                        evtHandler
                                .getParentExtension()
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
            evtHandler
                    .getParentExtension()
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
                                    player));
            currentAngle -= inc;
        }

        ISFSObject newShot = new SFSObject();
        newShot.putInt("playerId", params.getInt("playerId"));
        newShot.putInt("msgType", EvtEnum.EVT_SEND_SHOT_POSITION.code);
        newShot.putFloat("angle", angle);
        newShot.putFloat("inc", inc);
        newShot.putFloat("x", x);
        newShot.putFloat("y", y);
        newShot.putInt("bnum", firstBnum);

        ISFSArray eventArray = new SFSArray();
        ISFSObject response = new SFSObject();
        eventArray.addSFSObject(newShot);
        response.putSFSArray("events", eventArray);
        evtHandler.respondToAll(response);
    }
}
