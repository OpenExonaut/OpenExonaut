package xyz.openexonaut.extension.room;

// TODO: stop the ~5-10s hang on other clients when user closes game

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import javax.swing.*;

import com.smartfoxserver.v2.core.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.entities.variables.*;
import com.smartfoxserver.v2.extensions.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.map.*;
import xyz.openexonaut.extension.exolib.messages.*;
import xyz.openexonaut.extension.room.eventhandlers.*;
import xyz.openexonaut.extension.room.reqhandlers.*;

public class ExonautRoomExtension extends SFSExtension {
    private ExoWorld world = null;

    private ExoWeapon[] weapons = null;
    private ExoProps exoProps = null;

    private final AtomicInteger nextBulletId = new AtomicInteger(1);
    private final AtomicInteger nextGrenadeId = new AtomicInteger(1);

    private ScheduledFuture<?> gameHandle = null;
    private ScheduledFuture<?> peekHandle = null;

    private Room room = null;

    private ExoPeek peek = null;

    private int timeLimit = 0;

    @Override
    public void init() {
        room = getParentRoom();
        exoProps =
                (ExoProps) getParentZone().getExtension().handleInternalMessage("getProps", null);

        world =
                new ExoWorld(
                        (ExoMap)
                                getParentZone()
                                        .getExtension()
                                        .handleInternalMessage(
                                                "getMap", room.getVariable("mapId").getIntValue()),
                        room,
                        exoProps);
        weapons =
                (ExoWeapon[])
                        getParentZone().getExtension().handleInternalMessage("getWeapons", null);
        timeLimit =
                room.getVariable("mode").getStringValue().equals("team")
                        ? exoProps.teamTime
                        : exoProps.soloTime;

        addEventHandler(SFSEventType.USER_JOIN_ROOM, UserJoinRoomHandler.class);
        addEventHandler(SFSEventType.USER_VARIABLES_UPDATE, UserVariableUpdateHandler.class);

        addRequestHandler("evt", EvtHandler.class);
        addRequestHandler("gef", GefHandler.class);
        addRequestHandler("ping", PingHandler.class);
    }

    @Override
    public void destroy() {
        if (gameHandle != null) {
            gameHandle.cancel(false);
        }
        if (peekHandle != null) {
            peekHandle.cancel(false);
        }
        if (peek != null) {
            JFrame frame = peek.frame;
            peek.canvas.removeAll();
            peek = null;
            frame.dispose();
        }
        if (world != null) {
            world.destroy();
        }
        super.destroy();
    }

    // TODO: hook into actual functionality
    public void creditsXPRewardSolo() {
        List<User> sortedUsers = new ArrayList<>(room.getPlayersList());
        Collections.sort(
                sortedUsers,
                Comparator.nullsLast(
                        (a, b) ->
                                ((ExoPlayer) b.getProperty("ExoPlayer")).getHacks()
                                        - ((ExoPlayer) a.getProperty("ExoPlayer"))
                                                .getHacks())); // sort by descending hacks
        int mostHacks = ((ExoPlayer) sortedUsers.get(0).getProperty("ExoPlayer")).getHacks();

        int[] award = new int[sortedUsers.size()]; // indexed by unsorted ids
        for (User user : sortedUsers) {
            if (user != null) {
                int id = user.getPlayerId() - 1;
                int hacks = ((ExoPlayer) user.getProperty("ExoPlayer")).getHacks();
                award[id] = exoProps.creditsParticipation; // participation
                award[id] += hacks * exoProps.creditsPerHack; // hacks
                if (hacks == mostHacks) {
                    award[id] += exoProps.creditsWin; // winning
                }
            }
        }
    }

    private AtomicInteger getNextBulletId() {
        return nextBulletId;
    }

    private AtomicInteger getNextGrenadeId() {
        return nextGrenadeId;
    }

    private int getTimeLimit() {
        return timeLimit;
    }

    private ExoWeapon getWeapon(int weaponId) {
        return weapons[weaponId - 1];
    }

    private ExoItem[] getItems() {
        return world.items;
    }

    private boolean spawnBullet(ExoBullet bullet) {
        return world.spawnBullet(bullet);
    }

    private Object spawnPlayer(int id) {
        world.spawnPlayer(id);
        return null;
    }

    private Object startCountdown() {
        // client state update targets 8 Hz. i think that's too infrequent, so let's
        // start at 20 Hz and go from there
        gameHandle =
                sfsApi.getSystemScheduler()
                        .scheduleAtFixedRate(new ExoTimer(), 0, 50, TimeUnit.MILLISECONDS);
        return null;
    }

    private Object handleSnipe(ExoBullet bullet) {
        world.handleSnipe(bullet);
        return null;
    }

    private Object setVariables(ExoVariableUpdate update) {
        sfsApi.setUserVariables(update.user, update.variableList);
        return null;
    }

    private Object traceIt(ExoTraceArgs traceArgs) {
        trace(traceArgs.level, traceArgs.args);
        return null;
    }

    private ExoProps getProps() {
        return exoProps;
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
            case "getWeapon":
                return getWeapon((Integer) parameters);
            case "getItems":
                return getItems();
            case "spawnBullet":
                return spawnBullet((ExoBullet) parameters);
            case "spawnPlayer":
                return spawnPlayer((Integer) parameters);
            case "startCountdown":
                return startCountdown();
            case "handleSnipe":
                return handleSnipe((ExoBullet) parameters);
            case "setVariables":
                return setVariables((ExoVariableUpdate) parameters);
            case "trace":
                return traceIt((ExoTraceArgs) parameters);
            case "getProps":
                return getProps();
            default:
                trace(ExtensionLogLevel.ERROR, "Invalid internal message " + command);
                return null;
        }
    }

    private class ExoPeek extends JPanel implements Runnable {
        private JFrame frame = new JFrame("ExoPeek");
        private Container canvas = frame.getContentPane();

        public ExoPeek() {
            setPreferredSize(new Dimension((int) world.map.size.x, (int) world.map.size.y));
            canvas.add(this);
            frame.pack();
            frame.setResizable(false);
            frame.setVisible(true);
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            world.draw(g);
        }

        @Override
        public void run() {
            repaint();
        }
    }

    private class ExoTimer implements Runnable {
        private float queueTime = exoProps.queueWait;
        private float gameTime = 0;
        private long lastNano = System.nanoTime();

        @Override
        public void run() {
            long nano = System.nanoTime();
            float deltaTime = (nano - lastNano) / 1_000_000_000f;
            lastNano = nano;

            if (queueTime > 0f) {
                float oldQueueTime = queueTime;
                queueTime = Math.max(queueTime - deltaTime, 0f);

                if (queueTime == 0f) {
                    List<RoomVariable> stateUpdate = new ArrayList<>();
                    stateUpdate.add(new SFSRoomVariable("state", "play"));
                    sfsApi.setRoomVariables(null, room, stateUpdate);

                    if (world.map.scale != 0f) {
                        peek = new ExoPeek();
                        peekHandle =
                                sfsApi.getSystemScheduler()
                                        .scheduleAtFixedRate(peek, 25, 50, TimeUnit.MILLISECONDS);
                    }

                    ISFSObject timerUpdate = new SFSObject();
                    timerUpdate.putInt("queueTime", 0);
                    send("queueTime", timerUpdate, room.getPlayersList());
                } else if (Math.ceil(queueTime) < Math.ceil(oldQueueTime)) {
                    ISFSObject timerUpdate = new SFSObject();
                    timerUpdate.putInt("queueTime", (int) Math.ceil(queueTime));
                    send("queueTime", timerUpdate, room.getPlayersList());
                }
            } else {
                float oldGameTime = gameTime;
                gameTime = Math.max(gameTime - deltaTime, 0f);

                if (Math.floor(gameTime) > Math.floor(oldGameTime)) {
                    List<RoomVariable> timeUpdate = new ArrayList<>();
                    timeUpdate.add(new SFSRoomVariable("time", (int) Math.floor(gameTime)));
                    sfsApi.setRoomVariables(null, room, timeUpdate);
                }

                world.tick();

                // TODO: end match if time > timeLimit
                // TODO: end match if capture limit, for that matter
            }
        }
    }
}
