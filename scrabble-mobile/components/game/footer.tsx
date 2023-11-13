import { ReactElement, useEffect, useImperativeHandle, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { Appbar, Badge, FAB, Text, useTheme } from 'react-native-paper';

export function GameFooter({ lastAction, rackRef, chatRef, historyRef, footerRef }) {

  const { t } = useTranslation();
  const theme = useTheme();

  const [rightSideFooter, setRightSideFooter] = useState<ReactElement>();
  const [leftSideFooter, setLeftSideFooter] = useState<ReactElement>();
  const unreadMessagesCountRef = useRef<number>(0);
  const unreadActionsCountRef = useRef<number>(0);

  useEffect(() => {
    if (!lastAction) {
      return;
    }

    loadFooter();

    return () => {
    };
  }, [lastAction]);

  useImperativeHandle(footerRef, () => ({
    setUnreadMessagesCount: (count: number) => { setUnreadMessagesCount(count) },
    setUnreadActionsCount: (count: number) => { setUnreadActionsCount(count) },
  }));

  function loadFooter(): void {
    if (lastAction.gameStatus === 'IN_PROGRESS' || lastAction.gameStatus === 'LAST_ROUND') {
      setLeftSideFooter(createLeftSideFooter());
      setRightSideFooter(createRightSideFooter());
    } else {
      setLeftSideFooter(createLeftSideFooter());
      setRightSideFooter(null);
    }
  }

  function setUnreadMessagesCount(count: number): void {
    unreadMessagesCountRef.current = count;
    loadFooter();
  }

  function setUnreadActionsCount(count: number): void {
    unreadActionsCountRef.current = count;
    loadFooter();
  }

  function onPressMessages(): void {
    chatRef.current.onShow(true);
    setUnreadMessagesCount(0);
  };

  function onPressHistory(): void {
    historyRef.current.onShow(true);
    setUnreadActionsCount(0);
  };

  function createLeftSideFooter(): ReactElement {
    return (
      <View style={styles.footerLeft}>
        <View style={styles.informationButtonPanel}>
          <FAB
            icon="message-text-outline"
            color='#fff'
            size='small'
            style={styles.chatButton}
            onPress={() => onPressMessages()} />
          <Text style={styles.buttonLabel}>{t('game.footer.button.messages')}</Text>
          <Badge
            size={14}
            style={styles.buttonBadge}>
            {unreadMessagesCountRef.current}
          </Badge>
        </View>
        <View style={styles.informationButtonPanel}>
          <FAB
            icon="history"
            color='#fff'
            size='small'
            style={styles.historyButton}
            onPress={() => onPressHistory()} />
          <Text style={styles.buttonLabel}>{t('game.footer.button.history')}</Text>
          <Badge
            size={14}
            style={styles.buttonBadge}>
            {unreadActionsCountRef.current}
          </Badge>
        </View>
      </View>
    );
  }

  function createRightSideFooter(): ReactElement {
    return (
      <View style={styles.footerRight}>
        <View style={styles.actionButtonPanel}>
          <FAB
            icon="format-text-variant-outline"
            color='#fff'
            size='small'
            style={styles.exchangeButton}
            onPress={() => rackRef.current.exchange()} />
          <Text style={styles.buttonLabel}>{t('game.footer.button.exchange')}</Text>
        </View>
        <View style={styles.actionButtonPanel}>
          <FAB
            icon="refresh"
            color='#fff'
            size='small'
            style={styles.skipButton}
            onPress={() => rackRef.current.skip()} />
          <Text style={styles.buttonLabel}>{t('game.footer.button.skip')}</Text>
        </View>
        <View style={styles.actionButtonPanel}>
          <FAB
            icon="play-outline"
            color='#fff'
            size='small'
            style={styles.playButton}
            onPress={() => rackRef.current.play()} />
          <Text style={styles.buttonLabel}>{t('game.footer.button.play')}</Text>
        </View>
      </View>
    );
  }

  return (
    <View>
      <Appbar style={[styles.container, { backgroundColor: theme.colors.elevation.level2 }]}>
        {leftSideFooter}
        {rightSideFooter}
      </Appbar>
    </View>
  )
};

const styles = StyleSheet.create({
  container: {
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 6,
    paddingBottom: 2,
  },
  footerLeft: {
    flexDirection: 'row',
  },
  footerRight: {
    flexDirection: 'row',
  },
  informationButtonPanel: {
    minWidth: 50,
    alignItems: 'center',
    marginLeft: 10,
  },
  chatButton: {
    backgroundColor: '#1e7e34',
    justifyContent: 'center',
  },
  buttonLabel: {
    fontSize: 13,
    fontFamily: 'Playball-Regular',
  },
  buttonBadge: {
    backgroundColor: '#343a40',
    color: '#f8f9fa',
    fontSize: 10,
    fontFamily: 'Gilroy-Bold',
    position: 'absolute',
    top: -2,
  },
  historyButton: {
    backgroundColor: '#ff851b',
    justifyContent: 'center',
  },
  actionButtonPanel: {
    minWidth: 50,
    alignItems: 'center',
    marginRight: 10,
  },
  exchangeButton: {
    backgroundColor: '#d39e00',
    justifyContent: 'center',
  },
  skipButton: {
    backgroundColor: '#dc3545',
    justifyContent: 'center',
  },
  playButton: {
    backgroundColor: '#007bff',
    justifyContent: 'center',
  },
});
