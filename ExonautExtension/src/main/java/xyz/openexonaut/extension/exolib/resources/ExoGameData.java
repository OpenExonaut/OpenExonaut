package xyz.openexonaut.extension.exolib.resources;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import xyz.openexonaut.extension.exolib.data.*;

public class ExoGameData {
    private final ExoWeapon[] weapons;
    private final ExoMod[] mods;
    private final ExoSuit[] suits;

    public ExoGameData(JsonNode json) {
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
            mods[i] = new ExoMod(modData.get(i), this);
        }
        for (int i = 0; i < suits.length; i++) {
            suits[i] = new ExoSuit(suitData.get(i), this);
        }
    }

    public ExoWeapon getWeapon(int id) {
        return weapons[id - 1];
    }

    public ExoMod getMod(int id) {
        return mods[id - 1];
    }

    public ExoSuit getSuit(int id) {
        return suits[id - 1];
    }
}
