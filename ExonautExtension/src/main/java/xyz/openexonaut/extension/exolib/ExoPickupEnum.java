package xyz.openexonaut.extension.exolib;

public enum ExoPickupEnum {
    boost_armor(0),
    boost_damage(1),
    boost_invis(2),
    boost_speed(3),
    pickup_sniper(4),
    pickup_lobber(5),
    pickup_rockets(6),
    pickup_grenades(7),
    boost_random(8), // only picks the individual boosts (0 through 3)
    pickup_conspicuously_missing_id_from_the_client(9),
    boost_team_armor(10),
    boost_team_damage(11),
    boost_team_invis(12),
    boost_team_speed(13);

    public final int id;

    private ExoPickupEnum(int id) {
        this.id = id;
    }

    private static ExoPickupEnum[] pickups = null;

    public static ExoPickupEnum get(int ordinal) {
        if (pickups == null) {
            pickups = values();
        }
        return pickups[ordinal];
    }
}
