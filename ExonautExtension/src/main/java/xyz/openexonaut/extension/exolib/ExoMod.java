package xyz.openexonaut.extension.exolib;

import com.fasterxml.jackson.databind.*;

public class ExoMod {
    public final ExoWeapon weapon;
    public final String Name;
    public final int Num_Projectiles;
    public final float Projectile_Range;
    public final int Damage_Per_Projectile;

    public ExoMod(JsonNode node, ExoWeapon[] allWeapons) {
        this.weapon = allWeapons[node.get("WeaponID").asInt() - 1];
        this.Name = node.get("Name").asText();
        this.Num_Projectiles = node.get("Num_Projectiles").asInt();
        this.Projectile_Range = node.get("Projectile_Range").asInt();
        this.Damage_Per_Projectile = node.get("Damage_Per_Projectile").asInt();
    }
}
