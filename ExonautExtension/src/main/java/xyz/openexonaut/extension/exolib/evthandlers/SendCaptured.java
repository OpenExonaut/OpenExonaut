/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package xyz.openexonaut.extension.exolib.evthandlers;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;

import xyz.openexonaut.extension.exolib.game.*;

public class SendCaptured {
    public static final int msgType = 20;
    public final int bnum;
    public final int uAttackerID;

    public SendCaptured(Integer bnum, Integer uAttackerID) {
        this.bnum = bnum;
        this.uAttackerID = uAttackerID;
    }

    public static void handle(Room room, ExoPlayer player, ISFSObject params, String evtName) {
        ErrorReceipt.handle(room, player, params, evtName);
    }
}
