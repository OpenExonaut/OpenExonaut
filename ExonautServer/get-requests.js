const bcrypt = require('bcrypt');
const crypto = require('crypto');

module.exports = {
  handleLogin: function (username, password, token, collection) {
    return new Promise(function (resolve, reject) {
      collection
        .findOne({ 'user.TEGid': { $regex: new RegExp(`^${username}$`, 'i') } })
        .then((u) => {
          if (u != null) {
            bcrypt.compare(password, u.user.authpass, (err, res) => {
              if (res) {
                var expireDate = Date.parse(u.session.expires_at);
                if (token != '' && Date.now() < expireDate.valueOf()) {
                  if (u.session.token == token) {
                    resolve(u);
                  } else reject();
                } else {
                  var newToken = `${crypto.randomUUID()}`;
                  var today = new Date();
                  today.setDate(today.getDate() + 1);
                  var newSession = {
                    token: newToken,
                    expires_at: today,
                    renewable: false,
                  };
                  var newAuthID = `${crypto.randomBytes(48).toString('base64url')}`;
                  const update = {
                    $set: {
                      session: newSession,
                      'user.authid': newAuthID,
                    },
                  };
                  u.session = newSession;
                  u.user.authid = newAuthID;
                  collection
                    .updateOne(
                      {
                        'user.TEGid': {
                          $regex: new RegExp(`^${username}$`, 'i'),
                        },
                      },
                      update
                    )
                    .then((d) => {
                      resolve(u);
                    })
                    .catch(console.error);
                }
              } else {
                reject();
              }
            });
          } else reject('Null user');
        })
        .catch(console.error);
    });
  },
};
