package xyz.openexonaut.extension.exolib.enums;

import java.util.*;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.evthandlers.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public enum ExoEvtEnum {
    // ids with both send and receipt code
    EVT_SEND_SHOT_POSITION(
            SendMyShotPosition.msgType, SendMyShotPosition::handle), // SendMyShotPosition
    EVT_SEND_GRENADE_POSITION(
            SendMyGrenadePosition.msgType, SendMyGrenadePosition::handle), // SendMyGrenadePosition
    EVT_SEND_CHANGE_WEAPON(SendChangeWeapon.msgType, SendChangeWeapon::handle), // SendChangeWeapon
    EVT_SEND_PICKUP(
            SendActivatePickupEvent.msgType,
            SendActivatePickupEvent::handle), // SendActivatePickupEvent
    EVT_SEND_GRENADE_EXPLODE(
            SendGrenadeExplode.msgType, SendGrenadeExplode::handle), // SendGrenadeExplode
    EVT_SEND_ROCKET_EXPLODE(
            SendRocketExplode.msgType, SendRocketExplode::handle), // SendRocketExplode
    EVT_SEND_TAUNT(SendTaunt.msgType, SendTaunt::handle), // SendTaunt
    EVT_SEND_SNIPER_SHOT(SendSniperLine.msgType, SendSniperLine::handle), // SendSniperLine
    EVT_SEND_OPP_INFO(SendOpponentInfo.msgType, SendOpponentInfo::handle), // SendOpponentInfo
    EVT_TIME_UPDATE(SendTimeUpdate.msgType, SendTimeUpdate::handle), // SendTimeUpdate
    EVT_ACTIVE_BOOST_INFO(
            SendActiveBoostInfo.msgType, SendActiveBoostInfo::handle), // SendActiveBoostInfo

    // ids whose functions are called but have no receipt code (for server processing?)
    EVT_SEND_ROLL(SendRoll.msgType, SendRoll::handle), // SendRoll
    EVT_SEND_AIRDASH(SendAirdash.msgType, SendAirdash::handle), // SendAirdash
    EVT_SEND_FUEL_CONSUMED(
            SendFuelConsumed.msgType,
            SendFuelConsumed::handle), // SendFuelConsumed - sent when starting to recover fuel

    // ids which are never sent but have receipt code (probably server-sent or unused)
    EVT_SEND_SUIT_WEAPON(1, ErrorReceipt::handle), // not needed?
    EVT_SEND_DAMAGE(SendDamage.msgType, ErrorReceipt::handle),
    EVT_SEND_CAPTURED(SendCaptured.msgType, ErrorReceipt::handle),
    EVT_SEND_PICKUP_COMPLETE(SendPickupComplete.msgType, ErrorReceipt::handle),
    EVT_SEND_START_GAME(
            101, ErrorReceipt::handle), // not needed: minor GUI string also set by EVT_TIME_UPDATE

    // "server-sent" ids which just log
    EVT_SEND_BEEP(50, ErrorReceipt::handle),

    // ids whose functions aren't called and receipt code just logs
    EVT_SPAWN_PLAYER(40, ErrorReceipt::handle), // sendEventSpawnMe

    // ids whose functions aren't called and have no receipt code
    EVT_SEND_ACTION_POSITION(3, ErrorReceipt::handle), // SendMyActionPosition
    EVT_SEND_KILL_BULLET(14, ErrorReceipt::handle), // SendKillBullet
    EVT_OPP_HAS_DROP_IN(102, ErrorReceipt::handle), // SendOppReadyInMyGame
    EVT_CHAT(110, ErrorReceipt::handle), // sendChatMessage
    EVT_PLAYER_LOAD_PROGRESS(111, ErrorReceipt::handle), // sendMyLoadingStatus

    // otherwise unused ids which just "break;" in the receipt code
    EVT_SEND_RELEASED(21, ErrorReceipt::handle),
    EVT_DEV_START(120, ErrorReceipt::handle),
    EVT_ERROR_MESSAGE(200, ErrorReceipt::handle),

    // seemingly-unused ids
    EVT_SEND_POWER(7, ErrorReceipt::handle),
    EVT_SEND_OVERHEAT_JETPACK(11, ErrorReceipt::handle),
    EVT_SEND_HEAL(13, ErrorReceipt::handle),
    EVT_SEND_POWER_COMPLETE(19, ErrorReceipt::handle),
    EVT_SEND_DROP_DAMAGE_CLOUD(30, ErrorReceipt::handle),
    EVT_SEND_MAKE_VISIBLE(51, ErrorReceipt::handle),
    EVT_SEND_READY_TO_START(100, ErrorReceipt::handle),
    EVT_OPP_HAS_SUIT_LOADED(103, ErrorReceipt::handle);

    public final int code;
    private final EvtReceiver eventReceiver;

    private ExoEvtEnum(int code, EvtReceiver eventReceiver) {
        this.code = code;
        this.eventReceiver = eventReceiver;
    }

    private static final Map<Integer, ExoEvtEnum> valueMap = new TreeMap<>();

    static {
        for (ExoEvtEnum e : ExoEvtEnum.values()) {
            valueMap.put(e.code, e);
        }
    }

    public static void handleEvtReq(Room room, ExoPlayer player, ISFSObject params) {
        Integer msgType = ExoParamUtils.deserializeField(params, "msgType", Integer.class);

        if (msgType != null) {
            ExoEvtEnum evt = valueMap.get(msgType);
            evt.eventReceiver.handle(room, player, params, evt.toString());
        } else {
            params.putInt("msgType", -1);
            ErrorReceipt.handle(room, player, params, "__MSGTYPE_ABSENT");
        }
    }

    @FunctionalInterface
    private interface EvtReceiver {
        public void handle(Room room, ExoPlayer player, ISFSObject params, String evtName);
    }
}
