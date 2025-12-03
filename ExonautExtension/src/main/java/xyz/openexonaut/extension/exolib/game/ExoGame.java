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
    public final boolean teamRoom;
    public final int timeLimit;

    public final AtomicInteger nextBulletId = new AtomicInteger(1);
    public final AtomicInteger nextGrenadeId = new AtomicInteger(1);

    public final AtomicInteger banzaiHacks = new AtomicInteger(0);
    public final AtomicInteger atlasHacks = new AtomicInteger(0);

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
        teamRoom = room.getVariable("mode").getStringValue().equals("team");
        timeLimit = teamRoom ? ExoProps.getTeamTime() : ExoProps.getSoloTime();

        init();
    }

    public void init() {
        queueTime = ExoProps.getQueueWait();
        gameTime = 0f;
        banzaiHacks.set(0);
        atlasHacks.set(0);

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
                spawnPlayer(u);
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
    }

    public void spawnPlayer(User user) {
        world.spawnPlayer(user);

        int imbalance =
                room.getVariable("imbalance").getIntValue()
                        + (user.getVariable("faction").getStringValue().equals("banzai") ? 1 : -1);

        SFSRoomVariable imbalanceVariable = new SFSRoomVariable("imbalance", imbalance);
        imbalanceVariable.setHidden(true);
        setVariables(List.of(imbalanceVariable));

        if (room.getPlayersList().size() >= ExoProps.getMinPlayers()) {
            if (room.getVariable("state").getStringValue().equals("wait_for_min_players")) {
                setVariables(List.of(new SFSRoomVariable("state", "countdown")));
                prime();
                // client state update targets 8 Hz. i think that's too infrequent, so let's
                // start at 20 Hz and go from there
                gameHandle = scheduler.scheduleAtFixedRate(this, 0, 50, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void removePlayer(User user) {
        world.removePlayer(user);

        int imbalance =
                room.getVariable("imbalance").getIntValue()
                        - (user.getVariable("faction").getStringValue().equals("banzai") ? 1 : -1);

        SFSRoomVariable imbalanceVariable = new SFSRoomVariable("imbalance", imbalance);
        imbalanceVariable.setHidden(true);
        setVariables(List.of(imbalanceVariable));

        if (room.getPlayersList().size() < ExoProps.getMinPlayers()
                || (teamRoom && (imbalance > 1 || imbalance < -1))) {
            if (room.getVariable("state").getStringValue().equals("countdown")) {
                gameHandle.cancel(false);
                if (queueTime == 0f) {
                    // whoops, too late, carry on
                    gameHandle = scheduler.scheduleAtFixedRate(this, 50, 50, TimeUnit.MILLISECONDS);
                } else {
                    setVariables(List.of(new SFSRoomVariable("state", "wait_for_min_players")));
                    queueTime = ExoProps.getQueueWait();
                }
            }
        }
    }

    public int addTeamHack(boolean banzai) {
        return (banzai ? banzaiHacks : atlasHacks).incrementAndGet();
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
        boolean banzaiWon = banzaiHacks.get() > atlasHacks.get();
        boolean atlasWon = atlasHacks.get() > banzaiHacks.get();

        ISFSObject sendSummaryParams = new SFSObject();

        for (User user : sortedUsers) {
            if (user != null) {
                ISFSObject userSummary = new SFSObject();

                String tegID = (String) user.getProperty("tegid");
                ExoPlayer player = (ExoPlayer) user.getProperty("ExoPlayer");
                int hacks = player.getHacks();
                boolean banzai = user.getVariable("faction").getStringValue().equals("banzai");

                userSummary.putInt("nCaps", hacks);
                userSummary.putInt("nFalls", player.getCrashes());
                userSummary.putInt("nSaves", 0); // UNUSED: does nothing, don't know what it is

                int award = 0;
                award = ExoProps.getCreditsParticipation(); // participation
                award += hacks * ExoProps.getCreditsPerHack(); // hacks
                if ((teamRoom && ((banzai && banzaiWon) || (!banzai && atlasWon)))
                        || (!teamRoom && hacks == mostHacks)) {
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

                sendSummaryParams.putSFSObject(String.valueOf(user.getPlayerId(room)), userSummary);
            }
        }

        sendSummaryParams.putInt("BanzaiTotal", banzaiHacks.get());
        sendSummaryParams.putInt("AtlasTotal", atlasHacks.get());

        int mapId = (int) (Math.random() * ExoMapManager.getMapCount()) + 1;
        setVariables(
                List.of(
                        new SFSRoomVariable("mapId", mapId),
                        new SFSRoomVariable("lastMapLoadedId", mapId)));

        room.getExtension().send("sendSummary", sendSummaryParams, room.getPlayersList());

        queueTime = -10f;
    }

    @Override
    public float tick(ISFSArray eventQueue, Room room) {
        float deltaTime = super.tick(eventQueue, room);

        if (queueTime > 0f) {
            double oldQueueTime = Math.ceil(queueTime);
            queueTime = Math.max(queueTime - deltaTime, 0f);
            double ceiledQueueTime = Math.ceil(queueTime);

            if (queueTime == 0f) {
                setVariables(List.of(new SFSRoomVariable("state", "play")));

                world.prime();
                for (User user : room.getPlayersList()) {
                    ((ExoPlayer) user.getProperty("ExoPlayer")).prime();
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

            world.tick(eventQueue, room);

            if (flooredGameTime > (double) timeLimit) {
                stop("timeout");
            } else if (room.getPlayersList().size() < 2) {
                stop("playersleft");
            } else {
                int hackLimit = room.getVariable("hackLimit").getIntValue();
                boolean maxedCaptures =
                        teamRoom
                                && (banzaiHacks.get() >= hackLimit
                                        || atlasHacks.get() >= hackLimit);
                int remainingBanzai = 0;
                int remainingAtlas = 0;
                for (User user : room.getPlayersList()) {
                    if (teamRoom) {
                        if (user.getVariable("faction").getStringValue().equals("banzai")) {
                            remainingBanzai++;
                        } else {
                            remainingAtlas++;
                        }
                    } else {
                        if (((ExoPlayer) user.getProperty("ExoPlayer")).getHacks() >= hackLimit) {
                            maxedCaptures = true;
                            break;
                        }
                    }
                }
                if (maxedCaptures) {
                    stop("capturelimit");
                } else if (teamRoom) {
                    if (remainingBanzai == 0) {
                        stop("banzaileft");
                    } else if (remainingAtlas == 0) {
                        stop("atlasleft");
                    } else {
                        int imbalance = remainingBanzai - remainingAtlas;
                        if (imbalance > 1 || imbalance < -1) {
                            stop("teamimbalance");
                        }
                    }
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
        try {
            ISFSArray eventQueue = new SFSArray();
            tick(eventQueue, room);
            ExoSendUtils.sendEventArrayToAll(room, eventQueue);
        } catch (ExoRuntimeException e) {
            room.getExtension()
                    .handleInternalMessage(
                            "warnLog",
                            new ExoRuntimeException("room run sanitization exception", e));
        } catch (Exception e) {
            room.getExtension()
                    .handleInternalMessage(
                            "errorLog", new ExoRuntimeException("room run error", e));
        }
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
            try {
                repaint();
            } catch (ExoRuntimeException e) {
                room.getExtension()
                        .handleInternalMessage(
                                "warnLog",
                                new ExoRuntimeException("room evt sanitization exception", e));
            } catch (Exception e) {
                room.getExtension()
                        .handleInternalMessage(
                                "errorLog", new ExoRuntimeException("room evt error", e));
            }
        }
    }
}
