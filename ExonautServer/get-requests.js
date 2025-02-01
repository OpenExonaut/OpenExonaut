const bcrypt = require('bcrypt');
const crypto = require('crypto');

module.exports = {
  handleBrowserLogin: function (username, collection) {
    // /authenticate/user/{username} RETURNS username from database
    return new Promise(function (resolve, reject) {
      collection
        .findOne({ 'user.TEGid': { $regex: new RegExp(`^${username}$`, 'i') } })
        .then((data) => {
          resolve(data);
        })
        .catch((err) => {
          reject(err);
        });
    });
  },

  handleLogin: function (username, password, token, collection) {
    return new Promise(function (resolve, reject) {
      collection
        .findOne({ 'user.TEGid': { $regex: new RegExp(`^${username}$`, 'i') } })
        .then((user) => {
          if (user != null) {
            bcrypt.compare(password, user.user.authpass, (err, res) => {
              if (res) {
                var expireDate = Date.parse(user.session.expires_at);
                if (token != '' && Date.now() < expireDate.valueOf()) {
                  if (user.session.token == token) {
                    resolve(user);
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
                  const options = { upset: false };
                  const update = { $set: { session: newSession } };
                  user.session = newSession;
                  collection
                    .updateOne(
                      {
                        'user.TEGid': {
                          $regex: new RegExp(`^${username}$`, 'i'),
                        },
                      },
                      update,
                      options
                    )
                    .then((d) => {
                      resolve(user);
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
