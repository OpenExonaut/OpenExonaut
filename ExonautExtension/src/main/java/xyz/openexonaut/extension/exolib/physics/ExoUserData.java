/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package xyz.openexonaut.extension.exolib.physics;

import xyz.openexonaut.extension.exolib.game.*;

public class ExoUserData {
    public final ExoPlayer player;
    public final ExoBodyPart part;

    public ExoUserData(ExoPlayer player, ExoBodyPart part) {
        this.player = player;
        this.part = part;
    }
}
