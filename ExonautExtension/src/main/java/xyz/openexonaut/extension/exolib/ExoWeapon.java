package xyz.openexonaut.extension.exolib;

import com.fasterxml.jackson.databind.*;

public class ExoWeapon {
    public final String Name;
    public final int Projectiles;
    public final float Range;
    public final float Damage;
    public final float Radius1;
    public final float Radius1_Damage;
    public final float Radius2;
    public final float Radius2_Damage;
    public final float Velocity;

    public ExoWeapon(JsonNode node) {
        this.Name = node.get("Name").asText();
        this.Projectiles = node.get("Projectiles").asInt();
        this.Range = (float) node.get("Range").asDouble();
        this.Damage = (float) node.get("Damage").asDouble();
        this.Radius1 = (float) node.get("Radius1").asDouble();
        this.Radius1_Damage = (float) node.get("Radius1_Damage").asDouble();
        this.Radius2 = (float) node.get("Radius2").asDouble();
        this.Radius2_Damage = (float) node.get("Radius2_Damage").asDouble();
        this.Velocity = (float) node.get("Velocity").asDouble();
    }
}
