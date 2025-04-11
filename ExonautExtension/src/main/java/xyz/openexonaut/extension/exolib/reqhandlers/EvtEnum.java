package xyz.openexonaut.extension.exolib.reqhandlers;

import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.exolib.reqhandlers.evt.*;

public enum EvtEnum {
    // ids with both send and receipt code
    EVT_SEND_SHOT_POSITION(4, SendMyShotPosition::handle), // SendMyShotPosition
    EVT_SEND_GRENADE_POSITION(5, SendMyGrenadePosition::handle), // SendMyGrenadePosition
    EVT_SEND_CHANGE_WEAPON(6, Echo::handle), // SendChangeWeapon
    EVT_SEND_PICKUP(8, Echo::handle), // SendActivatePickupEvent TODO: pickup tracking
    EVT_SEND_GRENADE_EXPLODE(9, Echo::handle), // SendGrenadeExplode TODO: explosives
    EVT_SEND_ROCKET_EXPLODE(16, Echo::handle), // SendRocketExplode TODO: explosives
    EVT_SEND_TAUNT(17, Echo::handle), // SendTaunt
    EVT_SEND_SNIPER_SHOT(18, SendSniperLine::handle), // SendSniperLine
    EVT_SEND_OPP_INFO(23, SendOpponentInfo::handle), // SendOpponentInfo
    EVT_TIME_UPDATE(24, SendTimeUpdate::handle), // SendTimeUpdate
    EVT_ACTIVE_BOOST_INFO(25, Stub::handle), // SendActiveBoostInfo TODO: pickup tracking

    // ids whose functions are called but have no receipt code (for server processing?)
    EVT_SEND_ROLL(26, Stub::handle), // SendRoll TODO: player movement
    EVT_SEND_AIRDASH(27, Stub::handle), // SendAirdash TODO: player movement
    EVT_SEND_FUEL_CONSUMED(
            28, Stub::handle), // SendFuelConsumed - sent when starting to recover fuel TODO: fuel
    // tracking

    // ids which are never sent but have receipt code (probably server-sent or unused)
    EVT_SEND_SUIT_WEAPON(1, ErrorReceipt::handle), // not needed?
    EVT_SEND_DAMAGE(10, ErrorReceipt::handle),
    EVT_SEND_CAPTURED(20, ErrorReceipt::handle),
    EVT_SEND_PICKUP_COMPLETE(22, ErrorReceipt::handle), // not needed?
    EVT_SEND_START_GAME(
            101, ErrorReceipt::handle), // not needed: minor GUI string only which is also set by
    // EVT_TIME_UPDATE

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

    private EvtEnum(int code, EvtReceiver eventReceiver) {
        this.code = code;
        this.eventReceiver = eventReceiver;
    }

    public void handle(EvtHandler evtHandler, ExoPlayer player, ISFSObject params) {
        eventReceiver.handle(evtHandler, player, params);
    }

    public static EvtEnum getFromCode(int code) {
        for (EvtEnum e : EvtEnum.values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface EvtReceiver {
        public void handle(EvtHandler evtHandler, ExoPlayer player, ISFSObject params);
    }
}
