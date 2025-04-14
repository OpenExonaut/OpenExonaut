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
import xyz.openexonaut.extension.exolib.reqhandlers.*;
import xyz.openexonaut.extension.room.eventhandlers.*;

public class ExonautRoomExtension extends SFSExtension {
    private ExoWorld world = null;

    // private ExoSuit[] suits = null;
    private ExoWeapon[] weapons = null;

    private final AtomicInteger nextBulletId = new AtomicInteger(1);
    private final AtomicInteger nextGrenadeId = new AtomicInteger(1);

    private ScheduledFuture<?> gameHandle = null;
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
                        room);
        /*
        suits =
                (ExoSuit[])
                        this.getParentZone().getExtension().handleInternalMessage("getSuits", null);
        */
        weapons =
                (ExoWeapon[])
                        this.getParentZone()
                                .getExtension()
                                .handleInternalMessage("getWeapons", null);
        timeLimit = (room.getVariable("mode").getStringValue().equals("team")) ? 900 : 600;

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
        super.destroy();
    }

    // TODO: hook into actual functionality
    public void creditsXPRewardSolo() {
        List<User> sortedUsers = new ArrayList<>(room.getUserList());
        Collections.sort(
                sortedUsers,
                (a, b) ->
                        ((ExoPlayer) b.getProperty("ExoPlayer")).getHacks()
                                - ((ExoPlayer) a.getProperty("ExoPlayer"))
                                        .getHacks()); // sort by descending hacks
        int mostHacks = ((ExoPlayer) sortedUsers.get(0).getProperty("ExoPlayer")).getHacks();

        int[] award = new int[sortedUsers.size()]; // indexed by unsorted ids
        for (User user : sortedUsers) {
            int id = user.getPlayerId() - 1;
            int hacks = ((ExoPlayer) user.getProperty("ExoPlayer")).getHacks();
            award[id] = 5; // participation
            award[id] += hacks * 5; // hacks
            if (hacks == mostHacks) {
                award[id] += 10; // winning. for team matches, this is applied to everyone on the
                // winning team (team with most hacks)
            }
        }
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
            case "getWeapon":
                return weapons[(Integer) parameters - 1];
            case "getItems":
                return world.items;
            case "spawnBullet":
                return world.spawnBullet((ExoBullet) parameters);
            case "spawnPlayer":
                world.spawnPlayer((Integer) parameters);
                return null;
            case "startCountdown":
                // client state update targets 8 Hz. i think that's too infrequent, so let's
                // start at 20 Hz and go from there
                gameHandle =
                        sfsApi.getSystemScheduler()
                                .scheduleAtFixedRate(new ExoTimer(), 0, 50, TimeUnit.MILLISECONDS);
                return null;
            case "handleSnipe":
                world.handleSnipe((ExoBullet) parameters);
                return null;
            case "setVariable":
                Object[] params = (Object[]) parameters;
                ExoPlayer player = (ExoPlayer) params[0];
                String variable = (String) params[1];
                Object value = params[2];

                List<UserVariable> varUpdate = new ArrayList<>();
                varUpdate.add(new SFSUserVariable(variable, value));
                sfsApi.setUserVariables(player.user, varUpdate);
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
        private float queueTime = 10;
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
