const fs = require('fs');
const express = require('express');
const bodyParser = require('body-parser');
const app = express();
const cors = require('cors');

const getRequest = require('./get-requests.js');
const postRequest = require('./post-requests.js');

const SocketPolicyServer = require('./socket-policy.js');

const displayNames = require('./data/names.json');

function getLowerCaseName(name) {
  var firstLetter = name.charAt(name).toUpperCase();
  var fullString = firstLetter;
  fullString += name.toLowerCase().substring(1, name.length);
  return fullString;
}

function ensureConfFile(file, directory) {
  if (!fs.existsSync(directory + '/' + file)) {
    console.info('Copying default ' + file);
    fs.copyFileSync(file + '.example', directory + '/' + file);
  }
}

let config;
try {
  config = require('./config.js');
} catch (err) {
  if (err instanceof Error && err.code === 'MODULE_NOT_FOUND') {
    console.error(
      'FATAL: Could not find config.js. If this is your first time running the server,'
    );
    console.error(
      'copy config.js.example to config.js. You can then edit it to add your MongoDB URI'
    );
    console.error(
      'as well as customize other options. Once finished, restart the server.'
    );
  } else throw err;
  process.exit(1);
}

let gameData;
try {
  gameData = require('./static/exonaut/gamedata.json');
} catch (err) {
  if (err instanceof Error && err.code === 'MODULE_NOT_FOUND') {
    console.error(
      'FATAL: Could not find gamedata.json. Run the npm postinstall script.'
    );
  } else throw err;
  process.exit(1);
}

const { MongoClient, ServerApiVersion } = require('mongodb');
const mongoClient = new MongoClient(config.httpserver.mongouri, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
  serverApi: ServerApiVersion.v1,
});

mongoClient.connect((err) => {
  if (err) {
    console.error('FATAL: MongoDB connect failed: ' + err);
    process.exit(1);
  }

  const playerCollection = mongoClient.db('openexonaut').collection('users');

  ensureConfFile('client.props', 'static');
  ensureConfFile('crossdomain.xml', 'static');
  ensureConfFile('director.json', 'static'); // this file being pointed to by `client.props` is a minor hack
  ensureConfFile('News.txt', 'static');

  if (
    !fs.existsSync('static/exonaut-0.9.3.6119.unity3d') ||
    !fs.existsSync('static/suits')
  ) {
    console.warn(
      'WARN: Asset files missing from static folder - the game will not work properly.'
    );
    console.warn(
      "Please run 'npm run postinstall' to automatically download and extract them."
    );
  } else {
    ensureConfFile('event.txt', 'static/events');
  }

  app.set('view engine', 'ejs');
  app.use(express.static('static'));
  app.use(bodyParser.urlencoded({ extended: false }));
  app.use(bodyParser.json());
  app.use(cors());
  app.use(express.json());
  app.use(express.text());

  app.get('/', (req, res) => {
    res.render('index');
  });

  app.get('/register', (req, res) => {
    res.render('register', {
      displayNames: JSON.stringify(displayNames),
    });
  });

  app.get('/forgot', (req, res) => {
    res.render('forgot');
  });

  app.get('/login', (req, res) => {
    res.render('login');
  });

  app.post('/auth/register', (req, res) => {
    var nameCount = 0;
    if (
      req.body.name1 != '' &&
      displayNames.list1.includes(getLowerCaseName(req.body.name1))
    )
      nameCount++;
    else if (req.body.name1 != '') nameCount = -100;
    if (
      req.body.name2 != '' &&
      displayNames.list2.includes(getLowerCaseName(req.body.name2))
    )
      nameCount++;
    else if (req.body.name2 != '') nameCount = -100;
    if (
      req.body.name3 != '' &&
      displayNames.list3.includes(getLowerCaseName(req.body.name3))
    )
      nameCount++;
    else if (req.body.name3 != '') nameCount = -100;

    if (
      req.body.username != '' &&
      req.body.password != '' &&
      nameCount > 1 &&
      req.body.password == req.body.confirm
    ) {
      var names = [req.body.name1, req.body.name2, req.body.name3];
      postRequest
        .handleRegister(
          req.body.username,
          req.body.password,
          names,
          req.body.forgot,
          playerCollection
        )
        .then((u) => {
          if (u == 'login') {
            res.redirect('/login?exists=true');
          } else {
            res.cookie('TEGid', u.user.TEGid);
            res.cookie('authid', u.user.authid);
            res.cookie('dname', u.user.dname);
            res.cookie('authpass', u.user.authpass);
            var date = Date.parse(u.session.expires_at);
            res.cookie('session_token', u.session.token, {
              maxAge: date.valueOf() - Date.now(),
            });
            res.cookie('logged', true);
            res.redirect('/');
          }
        })
        .catch((e) => {
          console.log(e);
          res.redirect('/register?failed=true');
        });
    } else res.redirect('/register?failed=true');
  });

  app.post('/auth/forgot', (req, res) => {
    if (req.body.password == req.body.confirm) {
      postRequest
        .handleForgotPassword(
          req.body.username,
          req.body.forgot,
          req.body.password,
          playerCollection
        )
        .then((data) => {
          res.clearCookie('session_token');
          res.cookie('logged', false);
          res.redirect('/login');
        })
        .catch((e) => {
          console.log(e);
          res.redirect('/forgot?failed=true');
        });
    } else res.redirect('/forgot?failed=true');
  });

  app.get('/auth/login', (req, res) => {
    var session_token = '';
    for (var h of req.rawHeaders) {
      if (h.includes('session_token')) {
        var cookies = h.split(';');
        for (var c of cookies) {
          if (c.includes('session_token')) {
            session_token = c
              .replace('session_token=', '')
              .replace(' ', '')
              .replace(';', '');
          }
        }
      }
    }
    if (req.query.username != '' && req.query.password != '') {
      getRequest
        .handleLogin(
          req.query.username,
          req.query.password,
          session_token,
          playerCollection
        )
        .then((u) => {
          var date = Date.parse(u.session.expires_at);
          res.cookie('TEGid', u.user.TEGid, {
            maxAge: date.valueOf() - Date.now(),
          });
          res.cookie('authid', u.user.authid, {
            maxAge: date.valueOf() - Date.now(),
          });
          res.cookie('dname', u.user.dname, {
            maxAge: date.valueOf() - Date.now(),
          });
          res.cookie('authpass', u.user.authpass, {
            maxAge: date.valueOf() - Date.now(),
          });
          res.cookie('session_token', u.session.token, {
            maxAge: date.valueOf() - Date.now(),
          });
          res.cookie('logged', true, {
            maxAge: date.valueOf() - Date.now(),
          });
          res.redirect('/');
        })
        .catch((e) => {
          console.log(e);
          res.redirect('/login?failed=true');
        });
    }
  });

  app.post('/exonaut/ExonautPlayerAuthenticate', (req, res) => {
    postRequest
      .handleLogin(req.body.TEGid, req.body.authid, playerCollection)
      .then((data) => {
        res.send(data);
      })
      .catch((e) => {
        console.log(e);
      });
  });

  app.post('/exonaut/ExonautPlayerBuySuit', (req, res) => {
    postRequest
      .handlePurchase(
        req.body.TEGid,
        req.body.exId,
        req.body.buySuitId,
        playerCollection,
        gameData
      )
      .then((data) => {
        res.send(data);
      })
      .catch(console.error);
  });

  app.post('/exonaut/ExonautPlayerInstall', (req, res) => {
    postRequest
      .handleInstall(
        req.body.TEGid,
        req.body.login,
        req.body.suit,
        req.body.faction,
        playerCollection,
        gameData
      )
      .then((data) => {
        res.send(data);
      })
      .catch(console.error);
  });

  // TODO
  app.post('/exonaut/ExonautMetric', (req, res) => {
    postRequest
      .handleMetric()
      .then((data) => {
        res.send(data);
      })
      .catch(console.error);
  });

  // TODO
  app.post('/exonaut/ExonautPlayerGetMissionProgress', (req, res) => {
    postRequest
      .handleMissionProgress(req.body.exId)
      .then((data) => {
        res.send(data);
      })
      .catch(console.error);
  });

  app.listen(config.httpserver.port, () => {
    console.info(`Express server running on port ${config.httpserver.port}!`);
    if (config.sockpol.enable) {
      const policyContent = fs.readFileSync(config.sockpol.file, 'utf8');
      const sockpol = new SocketPolicyServer(
        config.sockpol.port,
        policyContent
      );
      sockpol.start(() => {
        console.info(`Socket policy running on port ${config.sockpol.port}!`);
      });
    }
  });
});
