package xyz.openexonaut.extension.exolib;

import com.fasterxml.jackson.databind.*;

public class ExoWeapon {
    public final String Name;
    public final int Projectiles;
    public final float Range;
    public final int Damage;
    public final float Radius1;
    public final int Radius1_Damage;
    public final float Radius2;
    public final int Radius2_Damage;
    public final float Velocity;

    public ExoWeapon (JsonNode node) {
        this.Name = node.get("Name").asText();
        this.Projectiles = node.get("Projectiles").asInt();
        this.Range = node.get("Range").asInt();
        this.Damage = node.get("Damage").asInt();
        this.Radius1 = node.get("Radius1").asInt();
        this.Radius1_Damage = node.get("Radius1_Damage").asInt();
        this.Radius2 = node.get("Radius2").asInt();
        this.Radius2_Damage = node.get("Radius2_Damage").asInt();
        this.Velocity = node.get("Velocity").asInt();
    }
}
