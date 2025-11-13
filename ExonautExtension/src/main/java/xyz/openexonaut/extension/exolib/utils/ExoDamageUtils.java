package xyz.openexonaut.extension.exolib.utils;

import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.resources.*;

public final class ExoDamageUtils {
    private ExoDamageUtils() {}

    public static float getDamageModifier(ExoPlayer player) {
        float modifier = 1f;
        if (player.getBoost() == ExoPickupEnum.boost_damage.id) {
            modifier += ExoProps.getBoostDamageMod();
        }
        if (player.getTeamBoost() == ExoPickupEnum.boost_team_damage.id) {
            modifier += ExoProps.getBoostTeamDamageMod();
        }
        return modifier;
    }
}
