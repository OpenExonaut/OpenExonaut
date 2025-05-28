package xyz.openexonaut.extension.exolib.data;

import com.fasterxml.jackson.databind.*;

public class ExoSuit {
    public final String Name;
    public final ExoMod WeaponMod;
    public final float Health;
    public final float Regen_Speed;
    public final float Regen_Delay;
    public final float Timer_Boosts;
    public final float Timer_Sp_Weps;
    public final float CoolDown_Sp_Weps;

    public ExoSuit(JsonNode node, ExoMod[] allMods) {
        this.Name = node.get("Name").asText();
        this.WeaponMod = allMods[node.get("WeaponMod").asInt() - 1];
        this.Health = (float) node.get("Health").asDouble();
        this.Regen_Speed = (float) node.get("Regen_Speed").asDouble();
        this.Regen_Delay = (float) node.get("Regen_Delay").asDouble();
        this.Timer_Boosts = (float) node.get("Timer_Boosts").asDouble();
        this.Timer_Sp_Weps = (float) node.get("Timer_Sp_Weps").asDouble();
        this.CoolDown_Sp_Weps = (float) node.get("CoolDown_Sp_Weps").asDouble();
    }
}
