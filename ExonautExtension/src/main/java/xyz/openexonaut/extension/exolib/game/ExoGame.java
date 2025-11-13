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
import xyz.openexonaut.extension.exolib.resources.ExoDB.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class ExoGame extends ExoTickable implements Runnable {
    public final int timeLimit;

    public final AtomicInteger nextBulletId = new AtomicInteger(1);
    public final AtomicInteger nextGrenadeId = new AtomicInteger(1);

    private final Room room;
    private final TaskScheduler scheduler;

    private float queueTime;
    private float gameTime;
    private ExoPeek peek = null;
    private ExoWorld world = null;

    private ScheduledFuture<?> gameHandle = null;
    private ScheduledFuture<?> peekHandle = null;

    public ExoGame(Room room) {
        this.room = room;
        this.scheduler = SmartFoxServer.getInstance().getTaskScheduler();

        timeLimit =
                room.getVariable("mode").getStringValue().equals("team")
                        ? ExoProps.getTeamTime()
                        : ExoProps.getSoloTime();

        init();
    }

    public void init() {
        queueTime = ExoProps.getQueueWait();
        gameTime = 0f;

        boolean reinit = room.containsVariable("stop");

        if (reinit) {
            destroy();

            List<RoomVariable> roomVars = new ArrayList<>(ExoEntryUtils.initialRoomVars);
            roomVars.add(new SFSRoomVariable("stop", null)); // delete variable
            setVariables(roomVars);

            for (User u : room.getPlayersList()) {
                ExoPlayer player = (ExoPlayer) u.getProperty("ExoPlayer");

                List<UserVariable> userVars = new ArrayList<>(ExoEntryUtils.initialUserVars);
                userVars.add(new SFSUserVariable("health", player.getSuit().Health));
                player.setVariables(userVars);

                player.reset();
            }

            queueTime += 10f;
        }

        world = new ExoWorld(ExoMapManager.getMap(room.getVariable("mapId").getIntValue()), room);

        if (reinit) {
            for (User u : room.getPlayersList()) {
                spawnPlayer(u.getPlayerId());
            }
        }
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
            world = null;
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

    public void stop(String reason) {
        setVariables(
                List.of(
                        new SFSRoomVariable("stop", reason),
                        new SFSRoomVariable("state", "stopped")));

        List<User> sortedUsers = new ArrayList<>(room.getPlayersList());
        Collections.sort(
                sortedUsers,
                Comparator.nullsLast(
                        (a, b) ->
                                ((ExoPlayer) b.getProperty("ExoPlayer")).getHacks()
                                        - ((ExoPlayer) a.getProperty("ExoPlayer"))
                                                .getHacks())); // sort by descending hacks
        int mostHacks = ((ExoPlayer) sortedUsers.get(0).getProperty("ExoPlayer")).getHacks();

        ISFSObject sendSummaryParams = new SFSObject();

        int banzaiTotal = 0;
        int atlasTotal = 0;
        for (User user : sortedUsers) {
            if (user != null) {
                ISFSObject userSummary = new SFSObject();

                String tegID = (String) user.getProperty("tegid");
                ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");
                int hacks = player.getHacks();

                userSummary.putInt("nCaps", hacks);
                userSummary.putInt("nFalls", player.getCrashes());
                userSummary.putInt("nSaves", 0); // UNUSED: does nothing, don't know what it is

                if (user.getVariable("faction").getStringValue().equals("banzai")) {
                    banzaiTotal += hacks;
                } else {
                    atlasTotal += hacks;
                }

                int award = 0;
                award = ExoProps.getCreditsParticipation(); // participation
                award += hacks * ExoProps.getCreditsPerHack(); // hacks
                if (hacks == mostHacks) {
                    // TODO: team winning condition
                    award += ExoProps.getCreditsWin(); // winning
                }

                // TODO: event award modifiers

                if (!tegID.equals("")) {
                    ExoPlayerDBUpdateOutput playerDBUpdate =
                            ExoDB.endOfMatchPlayerUpdate(
                                    tegID,
                                    new ExoPlayerDBUpdateInput(
                                            award, user.getVariable("suitId").getIntValue()));

                    userSummary.putInt("level", playerDBUpdate.level);
                    userSummary.putInt("totalXP", playerDBUpdate.xp);
                    userSummary.putInt("totalCred", playerDBUpdate.credits);
                    userSummary.putInt("battleXP", award);
                    userSummary.putInt("battleCred", award);
                    // TODO "Missions": fed into updateCompletedMissions
                    userSummary.putSFSObject("Missions", new SFSObject());
                } else {
                    userSummary.putInt("level", 1);
                    userSummary.putInt("totalXP", 0);
                    userSummary.putInt("totalCred", 0);
                    userSummary.putInt("battleXP", award); // seen in videos as being non-zero
                    userSummary.putInt("battleCred", 0);
                    userSummary.putSFSObject("Missions", new SFSObject());
                }

                sendSummaryParams.putSFSObject(String.valueOf(user.getPlayerId()), userSummary);
            }
        }

        sendSummaryParams.putInt("BanzaiTotal", banzaiTotal);
        sendSummaryParams.putInt("AtlasTotal", atlasTotal);

        int mapId = (int) (Math.random() * ExoMapManager.getMapCount()) + 1;
        setVariables(
                List.of(
                        new SFSRoomVariable("mapId", mapId),
                        new SFSRoomVariable("lastMapLoadedId", mapId)));

        room.getExtension().send("sendSummary", sendSummaryParams, room.getPlayersList());

        queueTime = -10f;
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

                for (User user : room.getPlayersList()) {
                    ((ExoPlayer) user.getProperty("ExoPlayer"))
                            .prime(); // otherwise, a long lobby queue can lead to immediate break
                    // out of crash sphere
                }

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
        } else if (!room.containsVariable("stop")) {
            double oldGameTime = Math.floor(gameTime);
            gameTime += deltaTime;
            double flooredGameTime = Math.floor(gameTime);

            if (flooredGameTime > oldGameTime) {
                setVariables(List.of(new SFSRoomVariable("time", (int) flooredGameTime)));
            }

            world.tick(eventQueue);

            if (flooredGameTime > (double) timeLimit) {
                stop("timeout");
            } else if (room.getPlayersList().size() < ExoProps.getMinPlayers()) {
                stop("playersleft");
            } // TODO: team termination circumstances
            else {
                boolean maxedCaptures = false;
                for (User user : room.getPlayersList()) {
                    if (((ExoPlayer) user.getProperty("ExoPlayer")).getHacks() >= 20) {
                        maxedCaptures = true;
                        break;
                    }
                }
                if (maxedCaptures) {
                    stop("capturelimit");
                }
            }
        } else {
            queueTime += deltaTime;

            if (queueTime >= 0f) {
                init();
            }
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

    public ExoWorld getWorld() {
        return world;
    }

    private class ExoPeek extends JPanel implements Runnable {
        public final JFrame frame = new JFrame("ExoPeek");
        public final Container canvas = frame.getContentPane();

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
}
