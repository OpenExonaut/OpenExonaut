package xyz.openexonaut.extension.exolib.data;

import java.util.*;

public class ExoProps {
    public final float headshotMod;
    public final float boostDamageMod;
    public final float boostTeamDamageMod;
    public final float boostArmorMod;
    public final float boostTeamArmorMod;

    public final int soloTime;
    public final int teamTime;
    public final int queueWait;
    public final int minPlayers;

    public final int creditsParticipation;
    public final int creditsPerHack;
    public final int creditsWin;

    public ExoProps(Properties props) {
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
}
