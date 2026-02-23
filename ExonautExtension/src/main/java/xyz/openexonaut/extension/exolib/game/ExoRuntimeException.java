/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package xyz.openexonaut.extension.exolib.game;

public class ExoRuntimeException extends RuntimeException {
    public ExoRuntimeException(String message) {
        super(message);
    }

    public ExoRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
