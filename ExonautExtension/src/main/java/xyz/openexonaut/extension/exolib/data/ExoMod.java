package xyz.openexonaut.extension.exolib.data;

import com.fasterxml.jackson.databind.*;

import xyz.openexonaut.extension.exolib.resources.*;

public class ExoMod {
    public final ExoWeapon weapon;
    public final String Name;
    public final int Num_Projectiles;
    public final float Projectile_Range;
    public final float Damage_Per_Projectile;

    public ExoMod(JsonNode node) {
        this.weapon = ExoGameData.getWeapon(node.get("WeaponID").asInt());
        this.Name = node.get("Name").asText();
        this.Num_Projectiles = node.get("Num_Projectiles").asInt();
        this.Projectile_Range = (float) node.get("Projectile_Range").asDouble();
        this.Damage_Per_Projectile = (float) node.get("Damage_Per_Projectile").asDouble();
    }
}
