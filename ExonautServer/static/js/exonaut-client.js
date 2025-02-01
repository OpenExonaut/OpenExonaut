var unity = null;

function isInternetExplorer() {
  if (window.document.documentMode) {
    return true;
  }
  return false;
}

function createParam(name, val) {
  var param = document.createElement('param');
  param.setAttribute('name', name);
  param.setAttribute('value', val);
  return param;
}

function logOut() {
  document.cookie = 'TEGid=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
  document.cookie = 'authid=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
  document.cookie = 'authpass=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
  document.cookie = 'dname=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
  document.cookie = 'logged=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
  document.cookie = 'session_token=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
  location.reload();
}

function embedUnity() {
  var object = document.createElement('object');
  object.setAttribute('classid', 'clsid:444785F1-DE89-4295-863A-D46C3A781394');
  object.setAttribute(
    'codebase',
    'undefined/UnityWebPlayer.cab#version=2,0,0,0'
  );
  object.setAttribute('id', 'unity-object');
  object.setAttribute('width', '100%');
  object.setAttribute('height', '100%');

  var params = {
    src: 'exonaut-0.9.3.6119.unity3d',
    bordercolor: 'FFFFFF',
    backgroundcolor: '000000',
    textcolor: 'FFFFFF',
    disableContextMenu: 'true',
    disablefullscreen: 'false',
    logoimage: 'exo_loader_crest.png',
    progressbarimage: 'exo_loader_bar.png', // Original did have these assets,
    progressframeimage: 'exo_loader_back.png', // but didn't use them in final
  };

  if (!isInternetExplorer()) {
    var embed = document.createElement('embed');
    embed.setAttribute('class', 'embed-responsive-item');
    embed.setAttribute('type', 'application/vnd.unity');
    embed.setAttribute('id', 'unity-embed');
    Object.keys(params).forEach(function (key) {
      embed.setAttribute(key, params[key]);
    });
  } else {
    Object.keys(params).forEach(function (key) {
      var paramToAppend = createParam(key, params[key]);
      object.appendChild(paramToAppend);
    });
  }

  var div = document.getElementById('embed-container');
  div.innerHTML = '';
  if (!isInternetExplorer()) {
    object.appendChild(embed);
    div.appendChild(object);
    unity = document.getElementById('unity-embed');
  } else {
    div.appendChild(object);
    unity = document.getElementById('unity-object');
  }
}

window.onload = function () {
  if (!isInternetExplorer()) {
    for (var i = 0; i < navigator.plugins.length; i++) {
      if (navigator.plugins[i].name.indexOf('Unity Player') != -1) {
        embedUnity();
      }
    }
  } else {
    try {
      var plugin = new ActiveXObject('UnityWebPlayer.UnityWebPlayer.1');
      embedUnity();
    } catch (e) {
      console.log('Failed to embed ActiveXObject');
    }
  }
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
  if (unity != null) {
    unity.style.width = unity.parentElement.width;
    unity.style.height = window.innerHeight - 56 + 'px';
  }
};

function Exonaut_CheckMSIBLoggedIn(name, callback) {
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
  unity.SendMessage(name, callback, returnOK);
}

function Exonaut_CheckMSIBAuthorized(name, callback) {
  unity.SendMessage(name, callback, 'true');
}

function Exonaut_GetCookies(name, callback) {
  unity.SendMessage(name, callback, document.cookie);
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
