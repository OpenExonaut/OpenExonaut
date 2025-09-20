package xyz.openexonaut.extension.exolib.data;

import com.fasterxml.jackson.databind.*;

public class ExoLevel {
    public final int ID;
    public final int Rank;
    public final int XP;

    public ExoLevel(JsonNode node) {
        this.ID = node.get("ID").asInt();
        this.Rank = node.get("Rank").asInt();
        this.XP = node.get("XP").asInt();
    }
}
