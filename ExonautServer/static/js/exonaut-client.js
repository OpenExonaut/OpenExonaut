/*
 * SPDX-FileCopyrightText: 2022-2024 OpenATBP Contributors, 2024-2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

function logOut() {
  document.cookie = 'TEGid=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
  document.cookie = 'authid=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
  document.cookie = 'authpass=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
  document.cookie = 'dname=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
  document.cookie = 'logged=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
  document.cookie = 'session_token=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
  location.reload();
}

window.onload = function () {
  OnResize();
  var cookies = document.cookie.split(';');
  var displayName = null;
  for (var i = 0; i < cookies.length; i++) {
    if (cookies[i].indexOf('dname') != -1) {
      displayName = cookies[i]
        .replace('dname=', '')
        .replace(' ', '')
        .replace(';', '');
    }
  }
  if (displayName != null) {
    document.getElementById('login-button').remove();
    document.getElementById('username-text').innerHTML =
      'Logged in as ' + decodeURI(displayName);
  } else {
    document.getElementById('logout-button').remove();
  }
};

var OnResize = function () {
  canvas.width = canvas.style.width = canvas.parentElement.clientWidth;
  canvas.height = canvas.style.height = window.innerHeight - 56 + 'px';
};

function Exonaut_CheckMSIBLoggedIn(name, callback) {
  console.log('login check!');

  var cookies = document.cookie.split(';');
  var returnOK = null;
  for (var i = 0; i < cookies.length; i++) {
    if (cookies[i].indexOf('logged') != -1) {
      returnOK = cookies[i]
        .replace('logged=', '')
        .replace(' ', '')
        .replace(';', '');
    }
  }
  console.log(returnOK);
  if (returnOK == null) returnOK = 'false';
  SendMessage(name, callback, returnOK);
}

function Exonaut_CheckMSIBAuthorized(name, callback) {
  SendMessage(name, callback, 'true');
}

function Exonaut_GetCookies(name, callback) {
  console.log('cookie check!');
  // Unity does not handle decoding the URL-encoded spaces
  var cookies = document.cookie.split(';');
  for (var i = 0; i < cookies.length; i++) {
    if (cookies[i].indexOf('dname') != -1) {
      cookies[i] = cookies[i].replace(/%20/g, ' ');
    }
  }
  SendMessage(name, callback, cookies.join(';'));
}

var AchievementUnityComm = {
  doUnityLoaded: function () {
    // stubbed
  },
  doSendStat: function () {
    // stubbed
  },
};

var LoginModule = {
  showLoginWindow: function () {
    window.location.href = '/login';
  },
};
