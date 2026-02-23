/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package xyz.openexonaut.extension.exolib.physics;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoBodyDef {
    public final boolean dynamic;
    public Exo2DVector position = Exo2DVector.ZERO;

    public ExoBodyDef(boolean dynamic) {
        this.dynamic = dynamic;
    }
}
