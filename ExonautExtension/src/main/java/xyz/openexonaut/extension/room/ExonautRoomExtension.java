package xyz.openexonaut.extension.room;

// TODO: stop the ~5-10s hang on other clients when user closes game

import java.util.concurrent.atomic.*;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.room.eventhandlers.*;
import xyz.openexonaut.extension.room.reqhandlers.*;

public class ExonautRoomExtension extends SFSExtension {
    private ExoGame game = null;

    @Override
    public void init() {
        game = new ExoGame(getParentRoom());

        addEventHandler(SFSEventType.USER_JOIN_ROOM, UserJoinRoomHandler.class);

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

    private boolean spawnBullet(ExoBullet bullet) {
        return game.getWorld().spawnBullet(bullet);
    }

    private Object spawnPlayer(int id) {
        game.spawnPlayer(id);
        return null;
    }

    private Object handleSnipe(ExoBullet bullet) {
        game.getWorld().handleSnipe(bullet);
        return null;
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
            case "spawnPlayer":
                return spawnPlayer((Integer) parameters);
            case "handleSnipe":
                return handleSnipe((ExoBullet) parameters);
            default:
                throw new RuntimeException(
                        String.format("Invalid internal room message %s", command));
        }
    }
}
