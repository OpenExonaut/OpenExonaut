package xyz.openexonaut.extension.exolib.resources;

import java.util.*;
import java.util.regex.*;

public final class ExoProps {
    private static final String literalSemicolon = Pattern.quote(";");

    private static boolean inputDebug = false;

    private static float headshotMod = 0.25f;
    private static float boostDamageMod = 0.2f;
    private static float boostTeamDamageMod = 0.2f;
    private static float boostArmorMod = 0.2f;
    private static float boostTeamArmorMod = 0.2f;

    private static int maxHacksSolo = 20;
    private static int maxHacksTeam = 40;
    private static int soloTime = 600;
    private static int teamTime = 900;
    private static float[] queueWait = {-1f, -1f, -1f, -1f, -1f, 20f, 20f, 20f, 20f};
    private static float queueWaitLeastPlayers = 20f; // derived from queueWait

    private static int creditsParticipation = 5;
    private static int creditsPerHack = 5;
    private static int creditsWin = 10;

    private ExoProps() {}

    public static void init(Properties props) {
        inputDebug = Boolean.parseBoolean(props.getProperty("inputDebug"));

        headshotMod = Float.parseFloat(props.getProperty("headshotMod"));
        boostDamageMod = Float.parseFloat(props.getProperty("boostDamageMod"));
        boostTeamDamageMod = Float.parseFloat(props.getProperty("boostTeamDamageMod"));
        boostArmorMod = Float.parseFloat(props.getProperty("boostArmorMod"));
        boostTeamArmorMod = Float.parseFloat(props.getProperty("boostTeamArmorMod"));

        maxHacksSolo = Integer.parseInt(props.getProperty("maxHacksSolo"));
        maxHacksTeam = Integer.parseInt(props.getProperty("maxHacksTeam"));
        soloTime = Integer.parseInt(props.getProperty("soloTime"));
        teamTime = Integer.parseInt(props.getProperty("teamTime"));

        creditsParticipation = Integer.parseInt(props.getProperty("creditsParticipation"));
        creditsPerHack = Integer.parseInt(props.getProperty("creditsPerHack"));
        creditsWin = Integer.parseInt(props.getProperty("creditsWin"));

        String[] queueWaitStrings = props.getProperty("queueWait").split(literalSemicolon);
        boolean gotQueueWaitLeastPlayers = false;
        queueWait = new float[queueWaitStrings.length];
        for (int i = 0; i < queueWait.length; i++) {
            queueWait[i] = Float.parseFloat(queueWaitStrings[i]);
            if (!gotQueueWaitLeastPlayers && queueWait[i] >= 0f) {
                queueWaitLeastPlayers = queueWait[i];
                gotQueueWaitLeastPlayers = true;
            }
        }
    }

    public static boolean getInputDebug() {
        return inputDebug;
    }

    public static float getHeadshotMod() {
        return headshotMod;
    }

    public static float getBoostDamageMod() {
        return boostDamageMod;
    }

    public static float getBoostTeamDamageMod() {
        return boostTeamDamageMod;
    }

    public static float getBoostArmorMod() {
        return boostArmorMod;
    }

    public static float getBoostTeamArmorMod() {
        return boostTeamArmorMod;
    }

    public static int getMaxHacksSolo() {
        return maxHacksSolo;
    }

    public static int getMaxHacksTeam() {
        return maxHacksTeam;
    }

    public static int getSoloTime() {
        return soloTime;
    }

    public static int getTeamTime() {
        return teamTime;
    }

    public static float[] getQueueWait() {
        return queueWait;
    }

    public static float getQueueWaitLeastPlayers() {
        return queueWaitLeastPlayers;
    }

    public static int getCreditsParticipation() {
        return creditsParticipation;
    }

    public static int getCreditsPerHack() {
        return creditsPerHack;
    }

    public static int getCreditsWin() {
        return creditsWin;
    }
}
