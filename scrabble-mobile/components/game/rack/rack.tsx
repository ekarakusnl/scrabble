import { LinearGradient } from 'expo-linear-gradient';
import { ReactElement, useEffect, useImperativeHandle, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, TouchableOpacity, View } from 'react-native';
import { Badge } from 'react-native-paper';

import Icon from 'react-native-paper/src/components/Icon'

import GameService from '../../../services/game.service';
import VirtualRackService from '../../../services/virtual-rack.service';

import { Action } from '../../../model/action';
import { Cell } from '../../../model/cell';
import { DraggableTile } from '../../../model/draggable-tile';
import { DroppableZone } from '../../../model/droppable-zone';
import { GameStatus } from '../../../model/game-status';
import { RackTile } from './tile';
import { Tile } from '../../../model/tile';
import { VirtualRack } from '../../../model/virtual-rack';

const RACK_SIZE = 7;
const ON_EXCHANGE_OPACITY = 1;
const NOT_ON_EXCHANGE_OPACITY = 0.2;

export function Rack({ game, lastAction, viewingPlayer, rackRef, boardRef, boardZoneRef, notificationRef }) {

  const { t } = useTranslation();

  const [tiles, setTiles] = useState<ReactElement[]>([]);
  const [exchangedOpacity, setExchangedOpacity] = useState<number>(0.2);
  const [exchangedTileCount, setExchangedTileCount] = useState<number>(0);
  const [showCleanExchangedButton, setShowCleanExchangedButton] = useState<boolean>(false);

  const lastActionRef = useRef<Action>();
  const actionInProgress = useRef<boolean>(false);
  const virtualRackRef = useRef<VirtualRack>();
  const exchangeLayoutRef = useRef<View>();
  const exchangeZoneRef = useRef<DroppableZone>();
  const exchangedTilesRef = useRef<Tile[]>([]);

  useEffect(() => {
    if (!game || !lastAction || !viewingPlayer || lastAction.gameStatus !== GameStatus.IN_PROGRESS) {
      return;
    }

    exchangedTilesRef.current = [];
    lastActionRef.current = lastAction;
    loadRack();

    return () => {
      virtualRackRef.current = null;
    };
  }, [lastAction]);

  useImperativeHandle(rackRef, () => ({
    updateTile: (tileNumber: number, cell: Cell) => { updateTile(tileNumber, cell) },
    resetTile: (tileNumber: number) => { resetTile(tileNumber) },
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
            hasTurn={viewingPlayer.playerNumber === lastActionRef.current.currentPlayerNumber && !tile.sealed && !tile.exchanged}
            onDragTile={onDragTile}
            onDropTile={onDropTile} />
        );
      } else {
        const emptyTile: Tile = {
          playerNo: null, number: tileNumber + 1, rowNumber: null, columnNumber: null, letter: null,
          value: null, vowel: false, roundNumber: null, sealed: false, exchanged: false,
        };
        tiles.push(
          <RackTile
            key={'tile_' + emptyTile.number}
            tile={emptyTile}
            hasTurn={false}
            onDragTile={null}
            onDropTile={null} />
        );
      }
    }

    // reset the exchanged tile count
    setExchangedTileCount(exchangedTilesRef.current.length);

    // update the tiles
    setTiles(tiles);
  }

  function updateTile(tileNumber: number, cell: Cell): void {
    const updatedTile = virtualRackRef.current.tiles.find(tile => tile.number === tileNumber);
    updatedTile.cellNumber = cell.cellNumber;
    updatedTile.rowNumber = cell.rowNumber;
    updatedTile.columnNumber = cell.columnNumber;
    updatedTile.sealed = true;
    update();
  }

  function resetTile(tileNumber: number): void {
    const resettedTile = virtualRackRef.current.tiles.find(tile => tile.number === tileNumber);
    resettedTile.cellNumber = null;
    resettedTile.rowNumber = null;
    resettedTile.columnNumber = null;
    resettedTile.sealed = false;
    update();
  }

  function play(): void {
    if (actionInProgress.current) {
      notificationRef.current.warning(t('game.user.action.in.progress'));
      return;
    }

    if (viewingPlayer.playerNumber !== lastActionRef.current.currentPlayerNumber) {
      notificationRef.current.warning(t('error.2007', { 0: lastActionRef.current.currentPlayerNumber }));
      return;
    }

    const anyTileSealed = virtualRackRef.current.tiles.some(tile => tile.sealed);
    if (!anyTileSealed) {
      notificationRef.current.warning(t('game.board.tile.locate'));
      return;
    }

    const anyTileExchanged = virtualRackRef.current.tiles.some(tile => tile.exchanged);
    if (anyTileExchanged) {
      notificationRef.current.warning(t('game.rack.exchange.empty'));
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

    if (viewingPlayer.playerNumber !== lastActionRef.current.currentPlayerNumber) {
      notificationRef.current.warning(t('error.2007', { 0: lastActionRef.current.currentPlayerNumber }));
      return;
    }

    const anyTileExchanged = virtualRackRef.current.tiles.some(tile => tile.exchanged);
    if (anyTileExchanged) {
      notificationRef.current.warning(t('game.rack.exchange.empty'));
      return;
    }

    actionInProgress.current = true;
    virtualRackRef.current.tiles.map((tile: Tile) => {
      tile.sealed = false;
      tile.cellNumber = null;
      tile.rowNumber = null;
      tile.columnNumber = null;
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

    if (viewingPlayer.playerNumber !== lastActionRef.current.currentPlayerNumber) {
      notificationRef.current.warning(t('error.2007', { 0: lastActionRef.current.currentPlayerNumber }));
      return;
    } else if (exchangedTilesRef.current.length === 0) {
      notificationRef.current.warning(t('game.rack.tile.remove'));
      return;
    } else if (virtualRackRef.current.exchanged) {
      notificationRef.current.warning(t('error.2014'));
      return;
    }

    const anyTileSealed = virtualRackRef.current.tiles.some(tile => tile.sealed);
    if (anyTileSealed) {
      notificationRef.current.warning(t('game.board.tile.remove'));
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

  function onDragTile(draggableTile: DraggableTile): void {
    if (viewingPlayer.playerNumber !== lastActionRef.current.currentPlayerNumber) {
      return;
    }

    const draggedTile = virtualRackRef.current.tiles.find(tile => tile.number === draggableTile.number);
    if (isBoardZone(draggableTile)) {
      draggableTile.number = draggedTile.number;
      draggableTile.letter = draggedTile.letter;
      draggableTile.value = draggedTile.value;
      boardRef.current.onDragTile(draggableTile);
    }

    if (isExchangeZone(draggableTile)) {
      setExchangedOpacity(ON_EXCHANGE_OPACITY);
    } else {
      setExchangedOpacity(NOT_ON_EXCHANGE_OPACITY);
    }
  }

  function onDropTile(draggableTile: DraggableTile): void {
    if (viewingPlayer.playerNumber !== lastActionRef.current.currentPlayerNumber) {
      return;
    }

    const draggedTile = virtualRackRef.current.tiles.find(tile => tile.number === draggableTile.number);
    if (!draggedTile.sealed && isExchangeZone(draggableTile)) {
      draggedTile.exchanged = true;
      exchangedTilesRef.current.push(draggedTile);
      update();
    } else if (isBoardZone(draggableTile)) {
      draggableTile.number = draggedTile.number;
      draggableTile.letter = draggedTile.letter;
      draggableTile.value = draggedTile.value;
      boardRef.current.onDropTile(draggableTile);
    }
    setExchangedOpacity(NOT_ON_EXCHANGE_OPACITY);
  }

  function isBoardZone(draggableTile: DraggableTile): boolean {
    if (!boardZoneRef.current) {
      return false;
    }

    const horizontalCenter = draggableTile.x + (draggableTile.width / 2);
    const verticalCenter = draggableTile.y + (draggableTile.height / 2);

    const boardZone = boardZoneRef.current;
    return (isTileBetweenHorizontal(horizontalCenter, boardZone.x, boardZone.x + boardZone.width)
      && isTileBetweenVertical(verticalCenter, boardZone.y, boardZone.y + boardZone.height));
  }

  function isExchangeZone(draggableTile: DraggableTile): boolean {
    const exchangeZone = exchangeZoneRef.current;
    return (isTileBetweenHorizontal(exchangeZone.x, draggableTile.x, draggableTile.x + draggableTile.width)
      || isTileBetweenHorizontal(exchangeZone.x + exchangeZone.width, draggableTile.x, draggableTile.x + draggableTile.width))
      && (isTileBetweenVertical(exchangeZone.y, draggableTile.y, draggableTile.y + draggableTile.height)
        || isTileBetweenVertical(exchangeZone.y + exchangeZone.height, draggableTile.y, draggableTile.y + draggableTile.height));
  }

  function isTileBetweenHorizontal(x: number, start: number, end: number): boolean {
    return start <= x && x <= end;
  }

  function isTileBetweenVertical(y: number, start: number, end: number): boolean {
    return start <= y && y <= end;
  }

  function onExchangeLayout(): void {
    if (exchangeLayoutRef.current) {
      const paddingHorizontal = 14;
      const paddingVertical = 10;
      exchangeLayoutRef.current.measure((fx, fy, width, height, px, py) => {
        exchangeZoneRef.current = {
          x: px + paddingHorizontal,
          y: py + paddingVertical,
          width: width - paddingHorizontal,
          height: height - paddingVertical
        };
      });
    }
  }

  function onPressExchanged(): void {
    if (viewingPlayer.playerNumber !== lastActionRef.current.currentPlayerNumber) {
      return;
    }

    if (!showCleanExchangedButton) {
      setShowCleanExchangedButton(true);
      setExchangedOpacity(ON_EXCHANGE_OPACITY);
    } else {
      setShowCleanExchangedButton(false);
      setExchangedOpacity(NOT_ON_EXCHANGE_OPACITY);
    }
  }

  function onPressCleanExchanged(): void {
    if (viewingPlayer.playerNumber !== lastActionRef.current.currentPlayerNumber) {
      return;
    }

    if (showCleanExchangedButton) {
      // reset the exchange status of the tiles
      exchangedTilesRef.current.forEach((tile: Tile) => {
        tile.exchanged = false;
      });

      // empty the exchanged tiles
      exchangedTilesRef.current = [];

      // reset the rack
      update();

      setShowCleanExchangedButton(false);
      setExchangedOpacity(NOT_ON_EXCHANGE_OPACITY);
    }
  }

  if (!game || !lastAction || lastAction.gameStatus !== GameStatus.IN_PROGRESS ||
    !virtualRackRef || !virtualRackRef.current || !tiles) {
    return null;
  }

  return (
    <LinearGradient
      colors={['#e3cfaa', '#7d5b42']}
      style={styles.rack}>
      {tiles}
      <View
        ref={(ref) => exchangeLayoutRef.current = ref}
        onLayout={() => { onExchangeLayout() }}
        style={styles.exchange}>
        <TouchableOpacity
          onPress={() => onPressExchanged()}
          style={{ opacity: exchangedOpacity }}>
          <Icon
            source="trash-can-outline"
            color='black'
            size={48} />
        </TouchableOpacity>
        <TouchableOpacity onPress={() => onPressCleanExchanged()}>
          <Badge
            size={16}
            style={[styles.exchangeBadge, { backgroundColor: showCleanExchangedButton ? 'red' : '#343a40' }]}>
            {showCleanExchangedButton ? 'X' : exchangedTileCount}
          </Badge>
        </TouchableOpacity>
      </View>
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
    alignItems: 'center',
    paddingTop: 6,
    paddingBottom: 6,
    marginTop: 4,
    marginLeft: 8,
    marginRight: 8,
  },
  exchange: {
    flexDirection: 'row',
  },
  exchangeBadge: {
    backgroundColor: '#343a40',
    color: '#f8f9fa',
    fontSize: 10,
    fontFamily: 'Gilroy-Bold',
    position: 'absolute',
    top: 0,
    right: 0,
  },
});
