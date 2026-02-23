/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

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
