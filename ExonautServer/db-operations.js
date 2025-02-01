const crypto = require('crypto');
const fs = require('node:fs');

var newUserFunction = function (
  username,
  displayName,
  authpass,
  forgot,
  collection,
  gameData
) {
  //Creates new user in web server and database
  return new Promise((fulfill, reject) => {
    var inventoryArray = [];
    var today = new Date();
    today.setDate(today.getDate() + 1);
    for (var suit of gameData.suits) {
      if (suit.Guest == '1') inventoryArray.push(suit.ID);
    }
    if (displayName.charAt(displayName.length - 1) == ' ')
      displayName = displayName.substring(0, displayName.length - 1);
    var playerFile = {
      user: {
        TEGid: `${username.toLowerCase()}`,
        dname: `${displayName}`,
        authid: `${crypto.randomBytes(48).toString('base64url')}`, // the Exonaut bundle uses this as the login password
        authpass: `${authpass}`,
      },
      session: {
        token: `${crypto.randomUUID()}`,
        expires_at: today,
        renewable: false,
      },
      player: {
        XP: 0,
        Level: 1,
        Rank: 1,
        Credits: 0,
        Faction: 0,
        LastSuit: 0,

        Sneak_Atlas: 0,
        Sneak_Banzai: 0,
        Hacks_Total: 0,
        Crashes_Total: 0,
        GamesPlayed_Battle: 0,
        GamesPlayed_TeamBattle: 0,
        GamesWon_Battle: 0,
        GamesWon_TeamBattle: 0,
        Hacks_AtlasTrinity: 0,
        Hacks_BanzaiTrinity: 0,
        DaysPlayedInARow: 0,
        GamesPlayedInARow: 0,
        Highest_XP_SingleGame: 0,
        Highest_Ratio_Battle_Win: 0.0,
        Highest_Ratio_TeamBattle_Win: 0.0,
        Highest_Ratio_Battle: 0.0,
        Highest_Ratio_TeamBattle: 0.0,
        Wins_Treehouse_Jake_Bubblegum: 0,
        Wins_Treehouse_Finn_Marceline: 0,
        Wins_Abysus_Rex_Bobo: 0,
        Wins_Abysus_VanKleiss_Skalamander: 0,
        Wins_BBB_JohnnyTest: 0,
        Wins_Perplex_UltHS_NRG_FourArms_UltCannonbolt: 0,
        Wins_Perplex_UltBC_UltEE_UltSF_Heatblast: 0,
        Wins_BBB_BlingBlingBoy: 0,
        Hacks_Invisible: 0,
        Hacks_Speed: 0,
        Hacks_DamageBoost: 0,
        Hacks_ArmorBoost: 0,
      },
      inventory: inventoryArray,
      friends: [],
      forgot: forgot,
      requests: [],
      address: 'newAccount',
      queue: {
        lastDodge: -1,
        queueBan: -1,
        dodgeCount: 0,
        timesOffended: 0,
      },
    };
    const opt = { upsert: true };
    const update = { $set: playerFile };
    const filter = { 'user.TEGid': username };
    collection
      .updateOne(filter, update, opt)
      .then(() => {
        //Creates new user in the db
        fulfill(playerFile);
      })
      .catch((err) => {
        reject(err);
      });
  });
};

module.exports = {
  createNewUser: newUserFunction,
};
