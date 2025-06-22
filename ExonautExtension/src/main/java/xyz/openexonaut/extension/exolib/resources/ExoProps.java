package xyz.openexonaut.extension.exolib.resources;

import java.util.*;

public final class ExoProps {
    private static boolean inputDebug = false;

    private static float headshotMod = 0.25f;
    private static float boostDamageMod = 0.2f;
    private static float boostTeamDamageMod = 0.2f;
    private static float boostArmorMod = 0.2f;
    private static float boostTeamArmorMod = 0.2f;

    private static int soloTime = 600;
    private static int teamTime = 900;
    private static int queueWait = 20;
    private static int minPlayers = 4;

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

        soloTime = Integer.parseInt(props.getProperty("soloTime"));
        teamTime = Integer.parseInt(props.getProperty("teamTime"));
        queueWait = Integer.parseInt(props.getProperty("queueWait"));
        minPlayers = Integer.parseInt(props.getProperty("minPlayers"));

        creditsParticipation = Integer.parseInt(props.getProperty("creditsParticipation"));
        creditsPerHack = Integer.parseInt(props.getProperty("creditsPerHack"));
        creditsWin = Integer.parseInt(props.getProperty("creditsWin"));
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

    public static int getSoloTime() {
        return soloTime;
    }

    public static int getTeamTime() {
        return teamTime;
    }

    public static int getQueueWait() {
        return queueWait;
    }

    public static int getMinPlayers() {
        return minPlayers;
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
