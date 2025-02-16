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

import xyz.openexonaut.extension.exolib.*;
import xyz.openexonaut.extension.room.eventhandlers.*;
import xyz.openexonaut.extension.room.reqhandlers.*;

public class ExonautRoomExtension extends SFSExtension {
    private ExoWorld world = null;

    private ExoSuit[] suits = null;
    private ExoWeapon[] weapons = null;

    private final ExoPlayer[] players = new ExoPlayer[8];

    private final AtomicInteger nextBulletId = new AtomicInteger(1);
    private final AtomicInteger nextGrenadeId = new AtomicInteger(1);

    private ScheduledFuture<?> timeHandle = null;
    private ScheduledFuture<?> physicsHandle = null;
    private ScheduledFuture<?> peekHandle = null;

    private Room room = null;

    private ExoPeek peek = null;

    private int timeLimit = 0;

    @Override
    public void init() {
        room = this.getParentRoom();
        world =
                new ExoWorld(
                        (ExoMap)
                                this.getParentZone()
                                        .getExtension()
                                        .handleInternalMessage(
                                                "getMap", room.getVariable("mapId").getIntValue()),
                        players);
        suits =
                (ExoSuit[])
                        this.getParentZone().getExtension().handleInternalMessage("getSuits", null);
        weapons =
                (ExoWeapon[])
                        this.getParentZone()
                                .getExtension()
                                .handleInternalMessage("getWeapons", null);
        timeLimit = (room.getVariable("mode").getStringValue().equals("team")) ? 900 : 600;

        addEventHandler(SFSEventType.USER_JOIN_ROOM, UserJoinRoomHandler.class);
        addEventHandler(SFSEventType.USER_LEAVE_ROOM, UserLeaveRoomHandler.class);
        addEventHandler(SFSEventType.USER_VARIABLES_UPDATE, UserVariableUpdateHandler.class);

        addRequestHandler("evt", EvtHandler.class);
        addRequestHandler("gef", GefHandler.class);
        addRequestHandler("ping", PingHandler.class);
    }

    @Override
    public void destroy() {
        if (timeHandle != null) {
            timeHandle.cancel(true);
        }
        if (physicsHandle != null) {
            physicsHandle.cancel(true);
        }
        if (peekHandle != null) {
            peekHandle.cancel(true);
        }
        if (peek != null) {
            JFrame frame = peek.frame;
            peek.canvas.removeAll();
            peek = null;
            frame.dispose();
        }
        super.destroy();
    }

    @Override
    public Object handleInternalMessage(String command, Object parameters) {
        switch (command) {
            case "getNextBulletId":
                return nextBulletId;
            case "getNextGrenadeId":
                return nextGrenadeId;
            case "getTimeLimit":
                return timeLimit;
            case "getPlayers":
                return players;
            case "getSuit":
                return suits[(Integer) parameters - 1];
            case "getWeapon":
                return weapons[(Integer) parameters - 1];
            case "spawnBullet":
                return world.spawnBullet((ExoBullet) parameters);
            case "spawnPlayer":
                world.spawnPlayer((Integer) parameters);
                return null;
            case "startCountdown":
                timeHandle =
                        getApi().getSystemScheduler()
                                .scheduleAtFixedRate(new ExoTimer(), 1, 1, TimeUnit.SECONDS);
                return null;
            case "handleSnipe":
                world.handleSnipe((ExoBullet) parameters);
                return null;
            default:
                trace(ExtensionLogLevel.ERROR, "Invalid internal message " + command);
                return null;
        }
    }

    private class ExoPeek extends JPanel implements Runnable {
        private JFrame frame = new JFrame("ExoPeek");
        private Container canvas = frame.getContentPane();

        public ExoPeek() {
            this.setPreferredSize(new Dimension((int) world.map.size.x, (int) world.map.size.y));
            canvas.add(this);
            frame.pack();
            frame.setResizable(false);
            frame.setVisible(true);
            this.repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            world.draw(g);
        }

        @Override
        public void run() {
            this.repaint();
        }
    }

    private class ExoTimer implements Runnable {
        private int qTimer = 10;
        private int gameTime = 0;

        @Override
        public void run() {
            if (qTimer > 0) {
                qTimer--;
                if (qTimer == 0) {
                    List<RoomVariable> stateUpdate = new ArrayList<>();
                    stateUpdate.add(new SFSRoomVariable("state", "play"));
                    sfsApi.setRoomVariables(null, room, stateUpdate);

                    if (world.map.scale != 0f) {
                        peek = new ExoPeek();
                    }

                    // client state update targets 8 Hz. i think that's too infrequent, so let's
                    // start at 20 Hz and go from there
                    physicsHandle =
                            sfsApi.getSystemScheduler()
                                    .scheduleAtFixedRate(
                                            new ExoPhysicsTicker(), 25, 50, TimeUnit.MILLISECONDS);
                    peekHandle =
                            sfsApi.getSystemScheduler()
                                    .scheduleAtFixedRate(peek, 50, 50, TimeUnit.MILLISECONDS);
                }
                ISFSObject timerUpdate = new SFSObject();
                timerUpdate.putInt("queueTime", qTimer);

                send("queueTime", timerUpdate, room.getPlayersList());
            } else {
                List<RoomVariable> timeUpdate = new ArrayList<>();
                timeUpdate.add(new SFSRoomVariable("time", ++gameTime));
                sfsApi.setRoomVariables(null, room, timeUpdate);

                for (int i = 0; i < players.length; i++) {
                    ExoPlayer player = players[i];
                    if (player != null) {
                        player.secondlyTick(sfsApi);
                    }
                }
                // TODO: end match if time > timeLimit
                // TODO: end match if capture limit, for that matter
            }
        }
    }

    private class ExoPhysicsTicker implements Runnable {
        private long lastNano = System.nanoTime();

        @Override
        public void run() {
            long nano = System.nanoTime();
            float deltaTime = (nano - lastNano) / 1_000_000_000f;
            lastNano = nano;

            world.simulate(deltaTime);
        }
    }
}
