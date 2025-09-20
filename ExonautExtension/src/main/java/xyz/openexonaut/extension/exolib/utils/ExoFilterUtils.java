package xyz.openexonaut.extension.exolib.utils;

public final class ExoFilterUtils {
    private ExoFilterUtils() {}

    /*
        Explanation of filter bits:
            - Category bits (short): What I am
                - As used in this project, there are three possibilities:
                    - 0xffff: the walls
                    - bit n set in lower byte: player, ID n + 1
                        - ex: 0x0001: Player 1
                    - bit n set in upper byte: weapon, used by player ID n + 1
                        - ex: 0x0100: Weapon used by Player 1
            - Mask bits (short): What I collide with
                - As used in this project, there are three possibilities:
                    - 0xffff: the walls
                    - lower byte clear, upper byte set except bit n: player, ID n + 1
                        - ex: 0xfe00: Player 1
                    - upper byte clear, lower byte set except bit n: weapon, used by player ID n + 1
                        - ex: 0x00fe: Weapon used by Player 1

        As such, players collide with the walls and with other players' weapons, but not with each other nor their own weapons.

        TODO: team filtering
    */

    public static short getPlayerCategory(int id) {
        return (short) (1 << (id - 1));
    }

    public static short getPlayerMask(int id) {
        return (short) (0xff00 ^ (0x100 << (id - 1)));
    }

    public static short getWeaponCategory(int userId) {
        return (short) (0x100 << (userId - 1));
    }

    public static short getWeaponMask(int userId) {
        return (short) (0x00ff ^ (1 << (userId - 1)));
    }
}
