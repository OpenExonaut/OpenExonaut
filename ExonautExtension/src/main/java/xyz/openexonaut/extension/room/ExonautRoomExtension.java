package xyz.openexonaut.extension.room;

import java.util.concurrent.atomic.*;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.evthandlers.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.room.eventhandlers.*;
import xyz.openexonaut.extension.room.reqhandlers.*;

public class ExonautRoomExtension extends SFSExtension {
    private ExoGame game = null;

    @Override
    public void init() {
        game = new ExoGame(getParentRoom());

        addEventHandler(SFSEventType.USER_JOIN_ROOM, UserJoinRoomHandler.class);
        addEventHandler(SFSEventType.USER_LEAVE_ROOM, UserLeaveRoomHandler.class);
        addEventHandler(SFSEventType.USER_DISCONNECT, UserDisconnectRoomHandler.class);

        addRequestHandler("evt", EvtHandler.class);
        addRequestHandler("gef", GefHandler.class);
        addRequestHandler("ping", PingHandler.class);
    }

    @Override
    public void destroy() {
        if (game != null) {
            game.destroy();
        }
        super.destroy();
    }

    private AtomicInteger getNextBulletId() {
        return game.nextBulletId;
    }

    private AtomicInteger getNextGrenadeId() {
        return game.nextGrenadeId;
    }

    private int getTimeLimit() {
        return game.timeLimit;
    }

    private ExoItem[] getItems() {
        return game.getWorld().items;
    }

    private Object spawnBullet(ExoBullet bullet) {
        game.getWorld().spawnBullet(bullet);
        return null;
    }

    private Object spawnGrenade(ExoGrenade bullet) {
        game.getWorld().spawnGrenade(bullet);
        return null;
    }

    private Object spawnPlayer(User user) {
        game.spawnPlayer(user);
        return null;
    }

    private Object removePlayer(User user) {
        game.removePlayer(user);
        return null;
    }

    private Object handleSnipe(ExoBullet bullet) {
        game.getWorld().handleSnipe(bullet);
        return null;
    }

    private boolean explodeGrenade(SendGrenadeExplode explosion) {
        return game.getWorld().explodeGrenade(explosion.num, explosion.x, explosion.y);
    }

    private boolean explodeRocket(SendRocketExplode explosion) {
        return game.getWorld().explodeRocket(explosion.num, explosion.x, explosion.y);
    }

    private int addTeamHack(boolean banzai) {
        return game.addTeamHack(banzai);
    }

    @Override
    public Object handleInternalMessage(String command, Object parameters) {
        switch (command) {
            case "getNextBulletId":
                return getNextBulletId();
            case "getNextGrenadeId":
                return getNextGrenadeId();
            case "getTimeLimit":
                return getTimeLimit();
            case "getItems":
                return getItems();
            case "spawnBullet":
                return spawnBullet((ExoBullet) parameters);
            case "spawnGrenade":
                return spawnGrenade((ExoGrenade) parameters);
            case "spawnPlayer":
                return spawnPlayer((User) parameters);
            case "removePlayer":
                return removePlayer((User) parameters);
            case "handleSnipe":
                return handleSnipe((ExoBullet) parameters);
            case "explodeGrenade":
                return explodeGrenade((SendGrenadeExplode) parameters);
            case "explodeRocket":
                return explodeRocket((SendRocketExplode) parameters);
            case "addTeamHack":
                return addTeamHack((Boolean) parameters);
            default:
                throw new RuntimeException(
                        String.format("Invalid internal room message %s", command));
        }
    }
}
