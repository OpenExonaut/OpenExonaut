package xyz.openexonaut.extension.exolib;

import com.fasterxml.jackson.databind.*;

public class ExoSuit {
    public final String Name;
    public final ExoMod WeaponMod;
    public final int Health;
    public final int Regen_Speed;
    public final int Regen_Delay;
    public final int Timer_Boosts;
    public final int Timer_Sp_Weps;
    public final int CoolDown_Sp_Weps;

    public ExoSuit(JsonNode node, ExoMod[] allMods) {
        this.Name = node.get("Name").asText();
        this.WeaponMod = allMods[node.get("WeaponMod").asInt() - 1];
        this.Health = node.get("Health").asInt();
        this.Regen_Speed = node.get("Regen_Speed").asInt();
        this.Regen_Delay = node.get("Regen_Delay").asInt();
        this.Timer_Boosts = node.get("Timer_Boosts").asInt();
        this.Timer_Sp_Weps = node.get("Timer_Sp_Weps").asInt();
        this.CoolDown_Sp_Weps = node.get("CoolDown_Sp_Weps").asInt();
    }
}
