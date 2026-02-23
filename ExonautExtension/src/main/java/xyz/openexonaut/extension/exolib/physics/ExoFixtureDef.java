/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package xyz.openexonaut.extension.exolib.physics;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoFixtureDef {
    public final ExoShape shape;

    public ExoFixtureDef(ExoShape shape) {
        this.shape = shape;
    }
}
