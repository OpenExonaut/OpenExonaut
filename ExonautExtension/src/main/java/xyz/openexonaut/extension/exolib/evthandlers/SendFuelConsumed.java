/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class SendFuelConsumed {
    public static final int msgType = 28;

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        Integer fuel = ExoParamUtils.deserializeField(params, "fuel", Integer.class);
        if (fuel == null) {
            ErrorReceipt.handle(room, player, params, evtName);
        } else {
            player.addFuelConsumed(fuel);
        }
    }
}
