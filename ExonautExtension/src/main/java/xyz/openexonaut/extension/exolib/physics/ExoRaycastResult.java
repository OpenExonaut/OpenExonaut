/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package xyz.openexonaut.extension.exolib.physics;

public class ExoRaycastResult {
    public final float x;
    public final float y;
    public final ExoFixture fixture;
    public final float dist;

    public ExoRaycastResult(float x, float y, ExoFixture fixture, float dist) {
        this.x = x;
        this.y = y;
        this.fixture = fixture;
        this.dist = dist;
    }
}
