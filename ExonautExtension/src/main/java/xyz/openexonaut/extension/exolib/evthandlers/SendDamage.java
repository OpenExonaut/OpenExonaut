package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;

public class SendDamage {
    public static final int msgType = 10;
    public final int bnum;
    public final int uAttackerID;
    public final float damage;
    public final int hs;
    public final float health;

    public SendDamage(Integer bnum, Integer uAttackerID, Float damage, Integer hs, Float health) {
        this.bnum = bnum;
        this.uAttackerID = uAttackerID;
        this.damage = damage;
        this.hs = hs;
        this.health = health;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        ErrorReceipt.handle(room, player, params, evtName);
    }
}
