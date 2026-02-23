/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: MIT
 */

package xyz.openexonaut.extension.exolib.physics;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoFixtureDef {
    public final ExoShape shape;

    public ExoFixtureDef(ExoShape shape) {
        this.shape = shape;
    }
}
