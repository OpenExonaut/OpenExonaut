package xyz.openexonaut.extension.exolib.enums;

import java.util.*;

public enum ExoPickupEnum {
    boost_armor(0, ExoPickupTypeEnum.individual_boost),
    boost_damage(1, ExoPickupTypeEnum.individual_boost),
    boost_invis(2, ExoPickupTypeEnum.individual_boost),
    boost_speed(3, ExoPickupTypeEnum.individual_boost),
    pickup_sniper(4, ExoPickupTypeEnum.weapon),
    pickup_lobber(5, ExoPickupTypeEnum.weapon),
    pickup_rockets(6, ExoPickupTypeEnum.weapon),
    pickup_grenades(7, ExoPickupTypeEnum.refill),
    boost_random(8, ExoPickupTypeEnum.individual_boost),
    pickup_conspicuously_missing_id_from_the_client(9, ExoPickupTypeEnum.unknown),
    boost_team_armor(10, ExoPickupTypeEnum.team_boost),
    boost_team_damage(11, ExoPickupTypeEnum.team_boost),
    boost_team_invis(12, ExoPickupTypeEnum.team_boost),
    boost_team_speed(13, ExoPickupTypeEnum.team_boost);

    public final int id;
    private final ExoPickupTypeEnum type;

    private ExoPickupEnum(int id, ExoPickupTypeEnum type) {
        this.id = id;
        this.type = type;
    }

    private static final ExoPickupEnum[] pickups = values();
    private static final ExoPickupEnum[] individualBoosts =
            Arrays.stream(pickups)
                    .filter(
                            a ->
                                    a.type.equals(ExoPickupTypeEnum.individual_boost)
                                            && !a.equals(boost_random))
                    .toArray(ExoPickupEnum[]::new);

    public static ExoPickupEnum get(int ordinal) {
        return pickups[ordinal];
    }

    public static int getRandomIndividualPickup() {
        return individualBoosts[(int) (Math.random() * individualBoosts.length)].id;
    }

    public static boolean isIndividualBoost(int id) {
        return id < pickups.length && pickups[id].type.equals(ExoPickupTypeEnum.individual_boost);
    }

    public static boolean isTeamBoost(int id) {
        return id < pickups.length && pickups[id].type.equals(ExoPickupTypeEnum.team_boost);
    }

    private enum ExoPickupTypeEnum {
        individual_boost,
        team_boost,
        weapon,
        refill,
        unknown
    }
}
