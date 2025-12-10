module.exports = {
  patchGameData: function (gameData) {
    // reduce elite suits price
    gameData.suits[52].Cost = '9999';
    gameData.suits[53].Cost = '9999';
    gameData.suits[54].Cost = '9999';
    gameData.suits[55].Cost = '9999';
  },
};
