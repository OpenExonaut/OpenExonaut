package xyz.openexonaut.extension.exolib.resources;

import java.util.*;

public final class ExoProps {
    private static float headshotMod;
    private static float boostDamageMod;
    private static float boostTeamDamageMod;
    private static float boostArmorMod;
    private static float boostTeamArmorMod;

    private static int soloTime;
    private static int teamTime;
    private static int queueWait;
    private static int minPlayers;

    private static int creditsParticipation;
    private static int creditsPerHack;
    private static int creditsWin;

    private ExoProps() {}

    public static void init(Properties props) {
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
