const bcrypt = require('bcrypt');
const dbOp = require('./db-operations.js');
const crypto = require('crypto');
const XMLWriter = require('xml-writer');

module.exports = {
  handleRegister: function (username, password, names, forgot, collection) {
    return new Promise(function (resolve, reject) {
      bcrypt.hash(password, 10, (err, hash) => {
        var name = '';
        for (var i in names) {
          name += names[i];
          if (i != names.length - 1 && names[i] != '') name += ' ';
        }
        collection
          .findOne({
            $or: [
              { 'user.TEGid': { $regex: new RegExp(`^${username}$`, 'i') } },
              {
                'user.dname': name
                  .replace('[DEV] ', '')
                  .replace('[EXODEV] ', ''),
              },
            ],
          })
          .then((u) => {
            if (u != null) {
              resolve('login');
            } else {
              bcrypt.hash(forgot, 10, (er, has) => {
                dbOp
                  .createNewUser(username, name, hash, has, collection)
                  .then((u) => {
                    resolve(u);
                  })
                  .catch(console.error);
              });
            }
          })
          .catch(console.error);
      });
    });
  },
  handleForgotPassword: function (username, forgot, password, collection) {
    return new Promise(function (resolve, reject) {
      collection
        .findOne({ 'user.TEGid': { $regex: new RegExp(`^${username}$`, 'i') } })
        .then((u) => {
          if (u != null) {
            bcrypt.compare(forgot, u.forgot, (err, res) => {
              if (res) {
                bcrypt.hash(password, 10, (err, hash) => {
                  var today = new Date();
                  today.setDate(today.getDate() + 1);
                  var newSession = {
                    token: `${crypto.randomUUID()}`,
                    expires_at: today,
                    renewable: false,
                  };
                  collection
                    .updateOne(
                      {
                        'user.TEGid': {
                          $regex: new RegExp(`^${username}$`, 'i'),
                        },
                      },
                      {
                        $set: {
                          session: newSession,
                          'user.authpass': hash,
                        },
                      }
                    )
                    .then((r) => {
                      resolve(u);
                    })
                    .catch((e) => {
                      reject(e);
                    });
                });
              } else reject('No user found');
            });
          } else reject('Null');
        })
        .catch((e) => {
          console.log(e);
          reject();
        });
    });
  },

  handleLogin: function (TEGid, authid, collection) {
    // /exonaut/ExonautPlayerAuthenticate PROVIDES TEGid, authid RETURNS a lot
    return new Promise(function (resolve, reject) {
      collection
        .findOne({
          'user.TEGid': `${TEGid}`,
          'user.authid': `${authid}`,
        })
        .then((u) => {
          if (u != null) {
            //User exists
            if (u.player.Faction == 0) {
              //User has not yet joined a faction
              resolve(
                `<result status="newplayer" TEGID="${TEGid}" LoginName="${authid}"/>`
              );
            } else {
              xw = new XMLWriter();
              xw.startElement('result')
                .writeAttribute('status', 'exoplayer')
                .writeAttribute('TEGID', TEGid)
                .writeAttribute('LoginName', authid)
                .writeAttribute(
                  'ID',
                  Number('0x' + u.session.token.split('-')[0].slice(0, -1))
                ) // needs to fit an i32. any better ideas?
                .writeAttribute('Faction', u.player.Faction)
                .writeAttribute('Level', u.player.Level)
                .writeAttribute('XP', u.player.XP)
                .writeAttribute('Credits', u.player.Credits)
                .writeAttribute('SessionID', u.session.token)
                .writeAttribute('LastSuit', u.player.LastSuit);

              xw.startElement('suitsOwned');
              for (suit of u.inventory) {
                xw.writeElement('suit', suit);
              }
              xw.endElement();

              // TODO
              xw.writeElement('missionsCompleted', '');

              // TODO
              xw.writeElement('missionsProgress', '');

              xw.endElement();
              resolve(xw.toString());
            }
          } else {
            //User does not exist
            resolve('<result status="guest"/>');
          }
        })
        .catch((err) => {
          reject(err);
        });
    });
  },
  handleInstall: function (
    TEGid,
    login,
    suitToEquip,
    faction,
    collection,
    gameData
  ) {
    // /exonaut/ExonautPlayerInstall PROVIDES TEGid, login, dname, suit, faction RETURNS a lot
    return new Promise(function (resolve, reject) {
      try {
        const foundSuit = gameData.suits.find(
          (suit) => suit.ID === suitToEquip
        );
        if (foundSuit.Guest == '1') {
          if (foundSuit.Faction == faction) {
            collection
              .findOne({ 'user.TEGid': TEGid, 'user.authid': login })
              .then((u) => {
                if (u.player.Faction == 0) {
                  collection
                    .updateOne(
                      { 'user.TEGid': TEGid, 'user.authid': login },
                      {
                        $push: { inventory: suitToEquip },
                        $set: {
                          'player.LastSuit': suitToEquip,
                          'player.Faction': faction,
                        },
                      }
                    )
                    .then(() => {
                      xw = new XMLWriter();
                      xw.startElement('result')
                        .writeAttribute('status', 'new')
                        .writeAttribute(
                          'id',
                          Number(
                            '0x' + u.session.token.split('-')[0].slice(0, -1)
                          )
                        );

                      xw.startElement('suitsOwned');
                      for (suit of u.inventory) {
                        xw.writeElement('suit', suit);
                      }
                      xw.endElement();

                      // TODO
                      xw.writeElement('missionsCompleted', '');

                      // TODO
                      xw.writeElement('missionsProgress', '');

                      xw.endElement();
                      resolve(xw.toString());
                    });
                } else {
                  reject(new Error('Player is already installed'));
                }
              });
          } else {
            reject(new Error('Suit does not belong to faction'));
          }
        } else {
          reject(new Error('Not a starting suit'));
        }
      } catch (err) {
        reject(err);
      }
    });
  },
  handlePurchase: function (TEGid, exId, suitToPurchase, collection, gameData) {
    // /exonaut/ExonautPlayerBuySuit PROVIDES TEGid, exId, buySuitId, toCharge=1 RETURNS result
    return new Promise(function (resolve, reject) {
      try {
        const foundSuit = gameData.suits.find(
          (suit) => suit.ID === suitToPurchase
        );
        if (foundSuit) {
          //TODO: This could be simplified
          const exIdRegex = new RegExp(`^${Number(exId).toString(16)}`, 'i');
          collection
            .findOne({
              'user.TEGid': TEGid,
              'session.token': { $regex: exIdRegex },
            })
            .then((u) => {
              if (u != null) {
                if (u.player.Credits >= foundSuit.Cost) {
                  collection
                    .updateOne(
                      {
                        'user.TEGid': TEGid,
                        'session.token': { $regex: exIdRegex },
                      },
                      { $inc: { 'player.Credits': foundSuit.Cost * -1 } }
                    )
                    .then(() => {
                      //Subtracts the credits from the player
                      collection
                        .updateOne(
                          {
                            'user.TEGid': TEGid,
                            'session.token': { $regex: exIdRegex },
                          },
                          { $push: { inventory: suitToPurchase } }
                        )
                        .then((r) => {
                          if (r.modifiedCount == 0) {
                            resolve('<result status="fail"/>');
                          } else {
                            resolve('<result status="success"/>');
                          }
                        });
                    });
                } else {
                  reject(new Error('Not enough credits'));
                }
              } else {
                reject(new Error('User not found'));
              }
            });
        } else {
          reject(new Error('Suit not found'));
        }
      } catch (err) {
        reject(err);
      }
    });
  },
  // TODO
  handleMetric: function () {
    // /exonaut/ExonautMetric PROVIDES SessionID, ID, WantToPlay, GuestOrLogin, InHangar, RequestBattle, RequestTeamBattle, InLobby, MatchReady, NewMatchStart, DropIntoMatch, MatchLeftQuit, MatchLeftError, MatchCompleted, sessionTime RETURNS result
    return new Promise(function (resolve, reject) {
      resolve('<result/>');
    });
  },
  // TODO
  handleMissionProgress: function (exId) {
    // /exonaut/ExonautPlayerGetMissionProgress PROVIDES exId RETURNS result
    return new Promise(function (resolve, reject) {
      resolve('<result status="fail"/>'); // client tolerates "fail"
    });
  },
};
