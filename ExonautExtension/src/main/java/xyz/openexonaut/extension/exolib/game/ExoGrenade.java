/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package xyz.openexonaut.extension.exolib.game;

import xyz.openexonaut.extension.exolib.utils.*;

public class ExoGrenade {
    public final int num;
    public final ExoPlayer player;
    public final int weaponId;
    public final float damageModifier;

    public ExoGrenade(int num, ExoPlayer player, int weaponId) {
        this.num = num;
        this.player = player;
        this.weaponId = weaponId;
        this.damageModifier = ExoDamageUtils.getDamageModifier(player);
    }
}
