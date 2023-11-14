import { LinearGradient } from 'expo-linear-gradient';
import { ReactElement, useEffect, useImperativeHandle, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet } from 'react-native';

import GameService from '../../../services/game.service';
import VirtualRackService from '../../../services/virtual-rack.service';

import { Tile } from '../../../model/tile';
import { VirtualRack } from '../../../model/virtual-rack';
import { RackTile } from './tile';

const RACK_SIZE = 7;

export function Rack({ game, lastAction, viewingPlayer, selectedTileRef, rackRef, notificationRef }) {

  const { t } = useTranslation();

  const [tiles, setTiles] = useState<ReactElement[]>();
  const actionInProgress = useRef<boolean>(false);
  const virtualRackRef = useRef<VirtualRack>();

  useEffect(() => {
    if (!game || !lastAction || !viewingPlayer || !(lastAction.gameStatus === 'IN_PROGRESS' || lastAction.gameStatus === 'LAST_ROUND')) {
      return;
    }

    loadRack();

    return () => {
      virtualRackRef.current = null;
    };
  }, [lastAction]);

  useImperativeHandle(rackRef, () => ({
    update: () => { update() },
    play: () => { play() },
    skip: () => { skip() },
    exchange: () => { exchange() },
  }));

  function loadRack(): void {
    const playerRoundNumber = (lastAction.currentPlayerNumber >= viewingPlayer.playerNumber) ? lastAction.roundNumber :
      lastAction.roundNumber > 1 ? lastAction.roundNumber - 1 : 1;
    VirtualRackService.get(game.id, playerRoundNumber).then((rack: VirtualRack) => {
      virtualRackRef.current = rack;
      update();
    });
  }

  function update(): void {
    if (!virtualRackRef.current) {
      return;
    }

    const tiles = [];
    for (var tileNumber = 0; tileNumber < RACK_SIZE; tileNumber++) {
      const tile = virtualRackRef.current.tiles.find(tile => tile.number === tileNumber + 1);
      if (tile) {
        tiles.push(
          <RackTile
            key={'tile_' + tile.number}
            tile={tile}
            onSelectTile={onSelectTile} />
        );
      } else {
        const emptyTile: Tile = {
          playerNo: null, number: tileNumber + 1, rowNumber: null, columnNumber: null,
          letter: null, value: null, vowel: false, roundNumber: null, sealed: false, selected: false
        };
        tiles.push(
          <RackTile
            key={'tile_' + emptyTile.number}
            tile={emptyTile}
            onSelectTile={onSelectTile} />
          );
      }
    }
    setTiles(tiles);
  }

  function play(): void {
    if (actionInProgress.current) {
      notificationRef.current.warning(t('game.user.action.in.progress'));
      return;
    }

    if (viewingPlayer.playerNumber !== lastAction.currentPlayerNumber) {
      notificationRef.current.warning(t('error.2007', { 0: lastAction.currentPlayerNumber }));
      return;
    }

    const anyTileSelected = virtualRackRef.current.tiles.some(tile => tile.sealed);
    if (!anyTileSelected) {
      notificationRef.current.warning(t('game.board.tile.locate'));
      return;
    }

    actionInProgress.current = true;
    GameService.play(game.id, virtualRackRef.current).then(() => {
      actionInProgress.current = false;
    }).catch((error) => {
      actionInProgress.current = false;
      notificationRef.current.error(error.toString());
    });
  }

  function skip(): void {
    if (actionInProgress.current) {
      notificationRef.current.warning(t('game.user.action.in.progress'));
      return;
    }

    if (viewingPlayer.playerNumber !== lastAction.currentPlayerNumber) {
      notificationRef.current.warning(t('error.2007', { 0: lastAction.currentPlayerNumber }));
      return;
    }

    actionInProgress.current = true;
    virtualRackRef.current.tiles.map((tile: Tile) => {
      tile.sealed = false;
    });
    update();
    GameService.play(game.id, virtualRackRef.current).then(() => {
      actionInProgress.current = false;
    }).catch((error) => {
      actionInProgress.current = false;
      notificationRef.current.error(error.toString());
    });
  }

  function exchange(): void {
    if (actionInProgress.current) {
      notificationRef.current.warning(t('game.user.action.in.progress'));
      return;
    }

    if (viewingPlayer.playerNumber !== lastAction.currentPlayerNumber) {
      notificationRef.current.warning(t('error.2007', { 0: lastAction.currentPlayerNumber }));
      return;
    } else if (!selectedTileRef.current) {
      notificationRef.current.warning(t('game.rack.tile.select'));
      return;
    } else if (virtualRackRef.current.exchanged) {
      notificationRef.current.warning(t('error.2014'));
      return;
    }

    actionInProgress.current = true;
    VirtualRackService.exchangeTile(game.id, selectedTileRef.current.number).then((tile: Tile) => {
      virtualRackRef.current.tiles[selectedTileRef.current.number - 1] = tile;
      update();
      selectedTileRef.current = null;
      actionInProgress.current = false;
    }).catch((error) => {
      actionInProgress.current = false;
      notificationRef.current.error(error.toString());
    });
  }

  function onSelectTile(tile: Tile): void {
    if (viewingPlayer.playerNumber != lastAction.currentPlayerNumber) {
      notificationRef.current.warning(t('error.2007', { 0: lastAction.currentPlayerNumber }));
      return;
    }

    if (tile.sealed) {
      // the tile already is sealed in the board
      selectedTileRef.current = null;
    } else if (selectedTileRef.current && selectedTileRef.current.number === tile.number) {
      // the selected tile is clicked again, deselect now
      selectedTileRef.current.selected = false;

      // reset the selected tile number
      selectedTileRef.current = null;

      // update the rack
      update();
    } else if (selectedTileRef.current && selectedTileRef.current.number !== tile.number) {
      // first deselect the previous selected tile
      selectedTileRef.current.selected = false;

      // now select the current selected tile
      tile.selected = true;

      // set the selected tile
      selectedTileRef.current = tile;

      // update the rack
      update();
    } else {
      // select the selected tile
      tile.selected = true;

      // set the selected tile
      selectedTileRef.current = tile;

      // update the rack
      update();
    }
  }

  if (!game
        || !lastAction
        || !(lastAction.gameStatus === 'IN_PROGRESS' || lastAction.gameStatus === 'LAST_ROUND')
        || !virtualRackRef
        || !virtualRackRef.current
        || !tiles) {
    return null;
  }

  return (
    <LinearGradient
      colors={['#e3cfaa', '#7d5b42']}
      style={styles.rack}>
      {tiles}
    </LinearGradient>
  )
};

const styles = StyleSheet.create({
  rack: {
    flexDirection: 'row',
    backgroundColor: '#bd9768',
    borderColor: '#7d5b42',
    borderTopLeftRadius: 7,
    borderTopRightRadius: 7,
    borderBottomLeftRadius: 3,
    borderBottomRightRadius: 3,
    borderWidth: 1,
    justifyContent: 'center',
    paddingTop: 6,
    paddingBottom: 6,
    marginTop: 4,
    marginLeft: 8,
    marginRight: 8,
  },
});
