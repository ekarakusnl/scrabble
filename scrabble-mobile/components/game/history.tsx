import Constants from 'expo-constants';
import { AVPlaybackStatus, Audio } from 'expo-av';
import moment from 'moment';
import { ReactElement, useEffect, useImperativeHandle, useRef, useState } from 'react';
import { Trans, useTranslation } from 'react-i18next';
import { ScrollView, StyleSheet, View } from 'react-native';
import { Avatar, Modal, Text } from 'react-native-paper';

import { MessageBox } from '../layout/messageBox';

import ActionService from '../../services/action.service';
import WordService from '../../services/word.service';

import { Action } from '../../model/action';
import { Word } from '../../model/word';

export function GameHistory({ game, lastAction, historyRef, notificationRef, footerRef }) {

  const { t } = useTranslation();

  const [visible, setVisible] = useState<boolean>(false);
  const [selectedWord, setSelectedWord] = useState<Word>();
  const [actionMessages, setActionMessages] = useState<ReactElement[]>();
  const [actionsSynced, setActionsSynced] = useState<boolean>(false);
  const [notificationSound, setNotificationSound] = useState<Audio.Sound>();

  const actionsRef = useRef<Action[]>([]);
  const wordsRef = useRef<Word[]>([]);
  const lastReadActionCountRef = useRef<number>(0);
  const userAvatarMapRef = useRef<Map<number, ReactElement>>(new Map<number, ReactElement>([]));
  const scrollViewRef = useRef<ScrollView>();
  const visibleRef = useRef<boolean>(false);

  useEffect(() => {
    if (!game || !lastAction) {
      return;
    }

    loadActions();
    loadWords();

    return () => {
      unsyncNotificationSound();
    };
  }, [lastAction]);

  useImperativeHandle(historyRef, () => ({
    onShow: () => { onShow() },
    onHide: () => { onHide() },
  }));

  function onShow(): void {
    // reset the last read action count when showing the history dialog
    lastReadActionCountRef.current = actionsRef.current.length;
    visibleRef.current = true;
    setVisible(true);
  }

  function onHide(): void {
    visibleRef.current = false;
    setVisible(false);
    setSelectedWord(null);
  }

  function unsyncNotificationSound(): void {
    if (notificationSound) {
      notificationSound.unloadAsync();
    }
  }

  function loadActions(): void {
    ActionService.list(game.id).then((actions: Action[]) => {
      setActionsSynced(true);
      if (actions && actions.length > actionsRef.current.length) {
        // play a sound for new actions
        if (actions.length > actionsRef.current.length) {
          playNotificationSound();
        }

        if (!visibleRef.current) {
          // update the unread actions count
          const unreadNewActionsCount = actions.length - lastReadActionCountRef.current;
          footerRef.current.setUnreadActionsCount(unreadNewActionsCount);
        } else if (visibleRef.current) {
          lastReadActionCountRef.current = actions.length;
        }

        actionsRef.current = actions;
        setActionMessages(createActionMessages());
      }
    }).catch(error => {
      notificationRef.current.error(error);
    });
  }

  function loadWords(): void {
    WordService.list(game.id).then((words: Word[]) => {
      wordsRef.current = words;
      setActionMessages(createActionMessages());
    }).catch(error => {
      notificationRef.current.error(error);
    });
  }

  async function playNotificationSound(): Promise<AVPlaybackStatus> {
    const { sound } = await Audio.Sound.createAsync(require('../../assets/sounds/notification.mp3'));
    setNotificationSound(sound);
    return await sound.playAsync();
  }

  function createActionMessages(): ReactElement[] {
    const actionMessages: ReactElement[] = [];
    actionsRef.current.map((action: Action) => {
      actionMessages.push(createActionMessage(action));
    });
    return actionMessages;
  }

  function createActionMessage(action: Action): ReactElement {
    return (
      <View
        key={'actionMessage_' + action.id}
        style={styles.actionCard}>
        {getUserAvatar(action.userId)}
        {getActionMessage(action)}
      </View>
    );
  }

  function getActionMessage(action: Action): ReactElement {
    if (action.type === 'CREATE') {
      return getStandardActionMessage(t('game.history.action.create'), action.lastUpdatedDate);
    } else if (action.type === 'JOIN') {
      return getStandardActionMessage(t('game.history.action.join'), action.lastUpdatedDate);
    } else if (action.type === 'LEAVE') {
      return getStandardActionMessage(t('game.history.action.leave'), action.lastUpdatedDate);
    } else if (action.type === 'START') {
      return getStandardActionMessage(t('game.history.action.start'), action.lastUpdatedDate);
    } else if (action.type === 'BONUS_BINGO') {
      return getBonusMessage(action);
    } else if (action.type === 'PLAY') {
      return getWordsMessage(action);
    } else if (action.type === 'EXCHANGE') {
      return getExchangeMessage(action);
    } else if (action.type === 'SKIP') {
      return getStandardActionMessage(t('game.history.action.skip'), action.lastUpdatedDate);
    } else if (action.type === 'TIMEOUT') {
      return getStandardActionMessage(t('game.history.action.timeout'), action.lastUpdatedDate);
    } else if (action.type === 'END') {
      return getStandardActionMessage(t('game.history.action.end'), action.lastUpdatedDate);
    } else {
      return null;
    }
  }

  function getStandardActionMessage(message: string, actionDate: Date): ReactElement {
    return (
      <Text
        variant='bodySmall'
        style={styles.actionText}>
        {message}
        <Text
          variant='bodySmall'
          style={styles.actionDate}>
          {' [' + moment(actionDate).format('HH:mm:ss') + ']'}
        </Text>
      </Text>
    );
  }

  function getWordsMessage(action: Action): ReactElement {
    if (!wordsRef.current) {
      return null;
    }

    const actionWords = wordsRef.current.filter((word: Word) => word.actionId === action.id);
    const dictionaryWords = actionWords.map((word) => getDictionaryWord(word));
    const actionMessage = actionWords.length === 1 ? 'game.history.action.played.word' : 'game.history.action.played.words';
    return (
      <Text style={styles.compositeAction}>
        <Trans
          i18nKey={actionMessage}
          values={{ score: action.score }}
          parent={Text}
          components={{
            bold: (
              <Text
                style={styles.bold}>
              </Text>
            )
          }}
          style={styles.wordMessageText} />
        {dictionaryWords}
        <Text
          variant='bodySmall'
          style={styles.actionDate}>
          {' [' + moment(action.lastUpdatedDate).format('HH:mm:ss') + ']'}
        </Text>
      </Text>
    );
  }

  function getBonusMessage(action: Action): ReactElement {
    return (
      <Text style={styles.compositeAction}>
        <Trans
          i18nKey={'game.history.action.bonus.bingo'}
          values={{ score: action.score }}
          parent={Text}
          components={{
            bold: (
              <Text
                style={styles.bold}>
              </Text>
            )
          }}
          style={styles.wordMessageText} />
        <Text
          variant='bodySmall'
          style={styles.actionDate}>
          {' [' + moment(action.lastUpdatedDate).format('HH:mm:ss') + ']'}
        </Text>
      </Text>
    )
  }

  function getExchangeMessage(action: Action): ReactElement {
    return (
      <Text style={styles.compositeAction}>
        <Trans
          i18nKey={'game.history.action.exchange'}
          values={{ exchangedTileCount: '?' }}
          parent={Text}
          components={{
            bold: (
              <Text
                style={styles.bold}>
              </Text>
            )
          }}
          style={styles.wordMessageText} />
        <Text
          variant='bodySmall'
          style={styles.actionDate}>
          {' [' + moment(action.lastUpdatedDate).format('HH:mm:ss') + ']'}
        </Text>
      </Text>
    )
  }

  function getDictionaryWord(word: Word): ReactElement {
    return (
      <Text
        key={'word_' + word.id}
        onPress={() => onPressWord(word)}
        style={styles.wordText}>
        {' ' + word.word + '(' + word.score + ')'}
      </Text>
    );
  }

  function getUserAvatar(userId: number): ReactElement {
    if (!userAvatarMapRef.current.has(userId)) {
      userAvatarMapRef.current.set(userId, createUserAvatar(userId));
    }
    return userAvatarMapRef.current.get(userId);
  }

  function createUserAvatar(userId: number): ReactElement {
    return (
      <Avatar.Image
        source={{ uri: Constants.expoConfig.extra.EXPO_PUBLIC_PROFILE_PICTURE_URL + userId + '?' + new Date().getTime() }}
        size={30}
        style={styles.avatar} />
    );
  }

  function onPressWord(word: Word): void {
    setSelectedWord(word.definition ? word : null);
  }

  return (
    <Modal
      visible={visible}
      onDismiss={() => onHide()}
      contentContainerStyle={styles.historyDialog}>
      <View>
        {
          !actionsSynced ?
            <MessageBox message={t('game.history.loading')} type='info' size={18} />
            :
            <ScrollView
              ref={view => (scrollViewRef.current = view)}
              onContentSizeChange={() => scrollViewRef.current.scrollToEnd({ animated: true })}
              style={styles.historyView}>
              {actionMessages}
            </ScrollView>
        }
        {
          selectedWord ?
            <MessageBox
              message={selectedWord.word + ' : ' + selectedWord.definition}
              type=''
              size={16} />
            : ''
        }
      </View>
    </Modal>
  )
};

const styles = StyleSheet.create({
  historyDialog: {
    backgroundColor: '#d8d8d8',
    borderColor: '#d8d8d8',
    borderWidth: 1,
    borderRadius: 7,
    padding: 20,
    margin: 20,
  },
  historyView: {
    maxHeight: 320,
    marginBottom: 10,
  },
  actionCard: {
    flexShrink: 1,
    flexDirection: 'row',
    backgroundColor: '#fff',
    borderColor: '#fff',
    borderWidth: 0.5,
    borderRadius: 7,
    alignItems: 'center',
    padding: 6,
    marginTop: 6,
  },
  avatar: {
    marginRight: 6,
  },
  actionText: {
    flexShrink: 1,
    fontSize: 13,
    fontFamily: 'Gilroy-Regular',
    paddingRight: 6,
  },
  actionDate: {
    fontSize: 11,
    fontFamily: 'Gilroy-RegularItalic',
  },
  compositeAction: {
    flexShrink: 1,
    flexDirection: 'row',
    alignItems: 'center',
  },
  wordText: {
    color: '#3c8dbc',
    fontSize: 12,
    fontFamily: 'Gilroy-Regular',
    fontWeight: 'bold',
    textDecorationLine: 'underline',
    marginLeft: 2,
  },
  wordMessageText: {
    fontSize: 13,
    fontFamily: 'Gilroy-Regular',
  },
  bold: {
    fontWeight: 'bold',
  }
});
