/*
 * SPDX-FileCopyrightText: 2025-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

module.exports = {
  patchGameData: function (gameData) {
    // reduce elite suits price
    gameData.suits[52].Cost = '9999';
    gameData.suits[53].Cost = '9999';
    gameData.suits[54].Cost = '9999';
    gameData.suits[55].Cost = '9999';
  },
};
