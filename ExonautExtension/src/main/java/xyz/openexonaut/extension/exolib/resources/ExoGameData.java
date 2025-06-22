package xyz.openexonaut.extension.exolib.resources;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import xyz.openexonaut.extension.exolib.data.*;

public final class ExoGameData {
    private static ExoWeapon[] weapons = new ExoWeapon[0];
    private static ExoMod[] mods = new ExoMod[0];
    private static ExoSuit[] suits = new ExoSuit[0];

    private ExoGameData() {}

    public static void init(JsonNode json) {
        ArrayNode weaponData = (ArrayNode) json.get("weapons");
        ArrayNode modData = (ArrayNode) json.get("mods");
        ArrayNode suitData = (ArrayNode) json.get("suits");

        weapons = new ExoWeapon[weaponData.size()];
        mods = new ExoMod[modData.size()];
        suits = new ExoSuit[suitData.size()];

        for (int i = 0; i < weapons.length; i++) {
            weapons[i] = new ExoWeapon(weaponData.get(i));
        }
        for (int i = 0; i < mods.length; i++) {
            mods[i] = new ExoMod(modData.get(i));
        }
        for (int i = 0; i < suits.length; i++) {
            suits[i] = new ExoSuit(suitData.get(i));
        }
    }

    public static ExoWeapon getWeapon(int id) {
        return weapons[id - 1];
    }

    public static ExoMod getMod(int id) {
        return mods[id - 1];
    }

    public static ExoSuit getSuit(int id) {
        return suits[id - 1];
    }
}
