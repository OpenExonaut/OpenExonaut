/*
 * SPDX-FileCopyrightText: 2026 OpenExonaut Contributors
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

/*
  NOTE: This is a modified copy of UnityProgress.js from the Unity 5.3.8 WebGL template.
  I am unsure how Unity licenses that file, so take the above licensing information as provisional.
*/

function UnityProgress(dom) {
  this.progress = 0.0;
  this.message = '';
  this.dom = dom;

  var parent = dom.parentNode;

  var background = document.createElement('div');
  background.style.background = '#000000';
  background.style.position = 'absolute';
  parent.appendChild(background);
  this.background = background;

  var logoImage = document.createElement('img');
  logoImage.src = 'exo_loader_crest.png';
  logoImage.style.position = 'absolute';
  parent.appendChild(logoImage);
  this.logoImage = logoImage;

  var sfs2xLogo = document.createElement('img');
  sfs2xLogo.src = 'img/sfs2xlogo.png';
  sfs2xLogo.style.position = 'absolute';
  parent.appendChild(sfs2xLogo);
  this.sfs2xLogo = sfs2xLogo;

  var progressFrame = document.createElement('img');
  progressFrame.src = 'exo_loader_back.png';
  progressFrame.style.position = 'absolute';
  parent.appendChild(progressFrame);
  this.progressFrame = progressFrame;

  var progressBar = document.createElement('img');
  progressBar.src = 'img/fullbar.png';
  progressBar.style.position = 'absolute';
  parent.appendChild(progressBar);
  this.progressBar = progressBar;

  var messageArea = document.createElement('p');
  messageArea.style.color = '#ffffff';
  messageArea.style.position = 'absolute';
  parent.appendChild(messageArea);
  this.messageArea = messageArea;

  this.SetProgress = function (progress) {
    if (this.progress < progress) this.progress = progress;
    this.messageArea.style.display = 'none';
    this.progressFrame.style.display = 'inline';
    this.progressBar.style.display = 'inline';
    this.sfs2xLogo.style.display = 'inline';
    this.Update();
  };

  this.SetMessage = function (message) {
    this.message = message;
    this.background.style.display = 'inline';
    this.logoImage.style.display = 'inline';
    this.progressFrame.style.display = 'none';
    this.progressBar.style.display = 'none';
    this.sfs2xLogo.style.display = 'none';
    this.Update();
  };

  this.Clear = function () {
    this.background.style.display = 'none';
    this.logoImage.style.display = 'none';
    this.progressFrame.style.display = 'none';
    this.progressBar.style.display = 'none';
    this.sfs2xLogo.style.display = 'none';
  };

  this.Update = function () {
    this.background.style.top = this.dom.offsetTop + 'px';
    this.background.style.left = this.dom.offsetLeft + 'px';
    this.background.style.width = this.dom.offsetWidth + 'px';
    this.background.style.height = this.dom.offsetHeight + 'px';

    var logoImg = new Image();
    logoImg.src = this.logoImage.src;
    var sfs2xLogoImg = new Image();
    sfs2xLogoImg.src = this.sfs2xLogo.src;
    var progressFrameImg = new Image();
    progressFrameImg.src = this.progressFrame.src;

    this.logoImage.style.top =
      this.dom.offsetTop +
      (this.dom.offsetHeight * 0.5 - logoImg.height * 0.5) +
      'px';
    this.logoImage.style.left =
      this.dom.offsetLeft +
      (this.dom.offsetWidth * 0.5 - logoImg.width * 0.5) +
      'px';
    this.logoImage.style.width = logoImg.width + 'px';
    this.logoImage.style.height = logoImg.height + 'px';

    var progressFrameTop =
      this.dom.offsetTop +
      (this.dom.offsetHeight * 0.5 + logoImg.height * 0.5 + 10);
    var progressFrameLeft =
      this.dom.offsetLeft +
      (this.dom.offsetWidth * 0.5 - progressFrameImg.width * 0.5);

    this.progressFrame.style.top = progressFrameTop + 'px';
    this.progressFrame.style.left = progressFrameLeft + 'px';
    this.progressFrame.width = progressFrameImg.width;
    this.progressFrame.height = progressFrameImg.height;

    this.progressBar.style.top = progressFrameTop + 12 + 'px';
    this.progressBar.style.left = progressFrameLeft + 42 + 'px';
    this.progressBar.width = 250 * Math.min(this.progress, 1);
    this.progressBar.height = 8;

    this.sfs2xLogo.style.top =
      this.dom.offsetTop +
      (this.dom.offsetHeight - sfs2xLogoImg.height - 10) +
      'px';
    this.sfs2xLogo.style.left =
      this.dom.offsetLeft +
      (this.dom.offsetWidth * 0.5 - sfs2xLogoImg.width * 0.5) +
      'px';
    this.sfs2xLogo.width = sfs2xLogoImg.width;
    this.sfs2xLogo.height = sfs2xLogoImg.height;

    this.messageArea.style.top = this.progressFrame.style.top;
    this.messageArea.style.left = 0;
    this.messageArea.style.width = '100%';
    this.messageArea.style.textAlign = 'center';
    this.messageArea.innerHTML = this.message;
  };

  this.Update();
}
