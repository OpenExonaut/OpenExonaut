const { DownloaderHelper } = require('node-downloader-helper');
const extract = require('extract-zip');
const fs = require('fs');
const path = require('path');

const url =
  'https://archive.org/download/openexonaut-20250131/openexonaut-20250131-assets.zip';
const dl = new DownloaderHelper(url, './static/');

dl.on('end', () => {
  console.log('Download completed, extracting...');
  try {
    extract('./static/openexonaut-20250131-assets.zip', {
      dir: path.resolve('./static/'),
    }).then(() => {
      console.log('Extraction completed.');
      fs.rmSync('./static/openexonaut-20250131-assets.zip');
      fs.renameSync('./static/exonaut/gamedata.json', './gamedata.json');
      fs.rmdirSync('./static/exonaut');
      console.log('Cleaned up.');
    });
  } catch (err) {
    console.error('Extraction failed: ' + err);
  }
});

dl.on('error', (err) => {
  console.error('Asset download failed - check Internet connection.', err);
});
dl.on('start', () => {
  console.log('Downloading game assets... this may take a while.');
});

if (
  !fs.existsSync('static/exonaut-0.9.3.6119.unity3d') ||
  !fs.existsSync('static/suits')
) {
  dl.start().catch((err) => console.error(err));
} else {
  console.log('Asset files already present, skipping download.');
}
