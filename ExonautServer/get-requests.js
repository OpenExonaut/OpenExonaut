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
                  var today = new Date();
                  today.setDate(today.getDate() + 1);
                  u.session = {
                    token: `${crypto.randomUUID()}`,
                    expires_at: today,
                    renewable: false,
                  };
                  u.user.authid = `${crypto.randomBytes(48).toString('base64url')}`;
                  const update = {
                    $set: {
                      session: u.session,
                      'user.authid': u.user.authid,
                    },
                  };
                  if (!u.email) {
                    u.email = update.$set.email = {
                      address: '',
                      confirmed: false,
                    };
                  }
                  if (!u.reset) {
                    u.reset = update.$set.reset = {
                      token: `${crypto.randomBytes(48).toString('base64url')}`,
                      expires_at: new Date(),
                      renewable: false,
                    };
                  }
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
  handleReset: function (TEGid, token, collection) {
    return new Promise(function (resolve, reject) {
      collection
        .findOne({ 'user.TEGid': TEGid, 'reset.token': token })
        .then((u) => {
          if (u != null) {
            if (Date.now() < Date.parse(u.reset.expires_at)) {
              resolve(u);
            } else reject('Expired reset request');
          } else reject('Reset not matching');
        })
        .catch(console.error);
    });
  },
  handleConfirmEmail: function (TEGid, token, collection) {
    return new Promise(function (resolve, reject) {
      collection
        .findOne({ 'user.TEGid': TEGid, 'reset.token': token })
        .then((u) => {
          if (u != null) {
            if (Date.now() < Date.parse(u.reset.expires_at)) {
              if (u.email && u.email.address != '' && !u.email.confirmed) {
                u.session.expires_at = u.reset.expires_at = new Date();
                collection
                  .updateOne(
                    {
                      'user.TEGid': u.user.TEGid,
                    },
                    {
                      $set: {
                        'reset.expires_at': u.reset.expires_at,
                        'session.expires_at': u.session.expires_at,
                        'email.confirmed': true,
                      },
                    }
                  )
                  .then((d) => {
                    resolve(u);
                  })
                  .catch(console.error);
              } else reject('Invalid state for email confirmation');
            } else reject('Expired confirm request');
          } else reject('Confirm not matching');
        })
        .catch(console.error);
    });
  },
};
