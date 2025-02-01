package xyz.openexonaut.extension.room.reqhandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.room.reqhandlers.evt.*;

public class EvtHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        trace(ExtensionLogLevel.DEBUG, "room evt from " + sender.toString());
        ExoPlayer player = (ExoPlayer)sender.getProperty("ExoPlayer");
        if (player == null) {
            trace(ExtensionLogLevel.WARN, "null player for user " + sender.getId() + " \"" + sender.getName() + "\"");
        }

        if (params.containsKey("msgType")) {
            switch (params.getInt("msgType")) {
                case 4:   // EVT_SEND_SHOT_POSITION - SendMyShotPosition
                    SendMyShotPosition.handle(this, player, params);
                    break;

                case 5:   // EVT_SEND_GRENADE_POSITION - SendMyGrenadePosition
                    SendMyGrenadePosition.handle(this, player, params);
                    break;

                case 23:  // EVT_SEND_OPP_INFO - SendOpponentInfo
                    SendOpponentInfo.handle(this, player, params);
                    break;

                case 24:  // EVT_TIME_UPDATE - SendTimeUpdate
                    SendTimeUpdate.handle(this, player, params);
                    break;

                // TODO: track inactive map pickups
                case 25:  // EVT_ACTIVE_BOOST_INFO - SendActiveBoostInfo
                    // none => no response
                    break;

                // TODO: track pickup duration
                case 8:   // EVT_SEND_PICKUP - SendActivatePickupEvent
                // falls through

                case 17:  // EVT_SEND_TAUNT - SendTaunt
                // falls through

                case 6:   // EVT_SEND_CHANGE_WEAPON - SendChangeWeapon
                case 9:   // EVT_SEND_GRENADE_EXPLODE - SendGrenadeExplode
                case 16:  // EVT_SEND_ROCKET_EXPLODE - SendRocketExplode
                case 18:  // EVT_SEND_SNIPER_SHOT - SendSniperLine
                    Echo.handle(this, player, params);
                    break;

                // ids whose functions are called but have no receipt code (for server processing?)
                case 26:  // EVT_SEND_ROLL - SendRoll
                case 27:  // EVT_SEND_AIRDASH - SendAirdash
                case 28:  // EVT_SEND_FUEL_CONSUMED - SendFuelConsumed - sent when starting to recover fuel
                    break;

                // TODO: ids which are never sent but have receipt code (probably server-sent or unused)
                case 1:   // EVT_SEND_SUIT_WEAPON - not needed?
                case 10:  // EVT_SEND_DAMAGE
                case 20:  // EVT_SEND_CAPTURED
                case 22:  // EVT_SEND_PICKUP_COMPLETE - not needed?
                case 101: // EVT_SEND_START_GAME - not needed: minor GUI string only which is also set by EVT_TIME_UPDATE

                // "server-sent" ids which just log
                case 50:  // EVT_SEND_BEEP

                // ids whose functions aren't called and receipt code just logs
                case 40:  // EVT_SPAWN_PLAYER - sendEventSpawnMe

                // ids whose functions aren't called and have no receipt code
                case 3:   // EVT_SEND_ACTION_POSITION - SendMyActionPosition
                case 14:  // EVT_SEND_KILL_BULLET - SendKillBullet
                case 102: // EVT_OPP_HAS_DROP_IN - SendOppReadyInMyGame
                case 110: // EVT_CHAT - sendChatMessage
                case 111: // EVT_PLAYER_LOAD_PROGRESS - sendMyLoadingStatus

                // otherwise unused ids which just "break;" in the receipt code
                case 21:  // EVT_SEND_RELEASED
                case 120: // EVT_DEV_START
                case 200: // EVT_ERROR_MESSAGE

                // seemingly-unused ids
                case 7:   // EVT_SEND_POWER
                case 11:  // EVT_SEND_OVERHEAT_JETPACK
                case 13:  // EVT_SEND_HEAL
                case 19:  // EVT_SEND_POWER_COMPLETE
                case 30:  // EVT_SEND_DROP_DAMAGE_CLOUD
                case 51:  // EVT_SEND_MAKE_VISIBLE
                case 103: // EVT_OPP_HAS_SUIT_LOADED

                default:
                    trace(ExtensionLogLevel.WARN, "unhandled event " + params.getInt("msgType") + " from playerId " + params.getInt("playerId") + ", sender " + sender.getName() + " (id " + sender.getId() + ")");
                    break;
            }
        }
    }

    public void respondToOne(ISFSObject response, User recipient) {
        this.send("sendEvents", response, recipient);
    }
    public void respondToAll(ISFSObject response) {
        this.send("sendEvents", response, this.getParentExtension().getParentRoom().getPlayersList());
    }
}
