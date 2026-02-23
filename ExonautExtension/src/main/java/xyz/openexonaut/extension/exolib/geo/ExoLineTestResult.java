/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: MIT
 */

package xyz.openexonaut.extension.exolib.geo;

public class ExoLineTestResult {
    public final float x;
    public final float y;
    public final float dist;

    public ExoLineTestResult(float x, float y, float dist) {
        this.x = x;
        this.y = y;
        this.dist = dist;
    }
}
