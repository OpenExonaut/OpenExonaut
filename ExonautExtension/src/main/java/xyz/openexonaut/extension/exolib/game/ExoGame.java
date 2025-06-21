package xyz.openexonaut.extension.exolib.game;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import javax.swing.*;

import com.smartfoxserver.v2.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.entities.variables.*;
import com.smartfoxserver.v2.util.*;

import xyz.openexonaut.extension.exolib.resources.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class ExoGame extends ExoTickable implements Runnable {
    public final ExoWorld world;
    public final int timeLimit;

    public final AtomicInteger nextBulletId = new AtomicInteger(1);
    public final AtomicInteger nextGrenadeId = new AtomicInteger(1);

    private final Room room;
    private final TaskScheduler scheduler;

    private float queueTime;
    private float gameTime = 0f;
    private ExoPeek peek = null;

    private ScheduledFuture<?> gameHandle = null;
    private ScheduledFuture<?> peekHandle = null;

    public ExoGame(Room room) {
        this.room = room;
        this.scheduler = SmartFoxServer.getInstance().getTaskScheduler();

        world = new ExoWorld(ExoMapManager.getMap(room.getVariable("mapId").getIntValue()), room);

        queueTime = ExoProps.getQueueWait();

        timeLimit =
                room.getVariable("mode").getStringValue().equals("team")
                        ? ExoProps.getTeamTime()
                        : ExoProps.getSoloTime();
    }

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
    }

    public void spawnPlayer(int id) {
        world.spawnPlayer(id);

        if (room.getPlayersList().size() >= ExoProps.getMinPlayers()) {
            if (room.getVariable("state").getStringValue().equals("wait_for_min_players")) {
                setVariables(List.of(new SFSRoomVariable("state", "countdown")));
                // client state update targets 8 Hz. i think that's too infrequent, so let's
                // start at 20 Hz and go from there
                gameHandle = scheduler.scheduleAtFixedRate(this, 0, 50, TimeUnit.MILLISECONDS);
            }
        }
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
                award[id] = ExoProps.getCreditsParticipation(); // participation
                award[id] += hacks * ExoProps.getCreditsPerHack(); // hacks
                if (hacks == mostHacks) {
                    award[id] += ExoProps.getCreditsWin(); // winning
                }
            }
        }
    }

    @Override
    public float tick(ISFSArray eventQueue) {
        float deltaTime = super.tick(eventQueue);

        if (queueTime > 0f) {
            double oldQueueTime = Math.ceil(queueTime);
            queueTime = Math.max(queueTime - deltaTime, 0f);
            double ceiledQueueTime = Math.ceil(queueTime);

            if (queueTime == 0f) {
                setVariables(List.of(new SFSRoomVariable("state", "play")));

                if (world.map.scale != 0f) {
                    peek = new ExoPeek();
                    peekHandle = scheduler.scheduleAtFixedRate(peek, 25, 50, TimeUnit.MILLISECONDS);
                }

                ISFSObject timerUpdate = new SFSObject();
                timerUpdate.putInt("queueTime", 0);
                room.getExtension().send("queueTime", timerUpdate, room.getPlayersList());
            } else if (ceiledQueueTime < oldQueueTime) {
                ISFSObject timerUpdate = new SFSObject();
                timerUpdate.putInt("queueTime", (int) ceiledQueueTime);
                room.getExtension().send("queueTime", timerUpdate, room.getPlayersList());
            }
        } else {
            double oldGameTime = Math.floor(gameTime);
            gameTime += deltaTime;
            double flooredGameTime = Math.floor(gameTime);

            if (flooredGameTime > oldGameTime) {
                setVariables(List.of(new SFSRoomVariable("time", (int) flooredGameTime)));
            }

            world.tick(eventQueue);

            // TODO: end match if time > timeLimit
            // TODO: end match if capture limit, for that matter
        }

        return deltaTime;
    }

    @Override
    public void run() {
        ISFSArray eventQueue = new SFSArray();
        tick(eventQueue);
        ExoSendUtils.sendEventArrayToAll(room, eventQueue);
    }

    private void setVariables(List<RoomVariable> variables) {
        SmartFoxServer.getInstance()
                .getAPIManager()
                .getSFSApi()
                .setRoomVariables(null, room, variables);
    }

    private class ExoPeek extends JPanel implements Runnable {
        private final JFrame frame = new JFrame("ExoPeek");
        private final Container canvas = frame.getContentPane();

        private ExoPeek() {
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
}
