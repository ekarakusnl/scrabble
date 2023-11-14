import { AVPlaybackStatus, Audio } from 'expo-av';
import moment from 'moment';
import { ReactElement, useEffect, useImperativeHandle, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ScrollView, StyleSheet, View } from 'react-native';
import { Avatar, IconButton, Modal, Text, TextInput } from 'react-native-paper';

import ChatService from '../../services/chat.service';

import { Chat } from '../../model/chat';
import { MessageBox } from '../layout/messageBox';

const colors: string[] = ['red', 'blue', 'orange', 'green'];

export function GameChat({ game, viewingPlayer, chatRef, notificationRef, footerRef }) {

  const { t } = useTranslation();

  const [visible, setVisible] = useState<boolean>(false);
  const [chatMessages, setChatMessages] = useState<ReactElement[]>();
  const [message, setMessage] = useState<string>();
  const [messagesSynced, setMessagesSynced] = useState<boolean>(false);
  const [sending, setSending] = useState<boolean>(false);

  const chatsRef = useRef<Chat[]>([]);
  const syncChatsRef = useRef<boolean>();
  const lastReadMessageCountRef = useRef<number>(0);
  const userAvatarMapRef = useRef<Map<number, ReactElement>>(new Map<number, ReactElement>([]));
  const userColorRef = useRef<Map<number, string>>(new Map<number, string>([]));
  const messageTextHeightRef = useRef<number>(0);
  const scrollViewRef = useRef<ScrollView>();
  const visibleRef = useRef<boolean>(false);
  const newMessageSoundRef = useRef<Audio.Sound>();

  useEffect(() => {
    if (!game || !viewingPlayer || !footerRef) {
      return;
    }

    subscribeChats();
    loadChats();

    return () => {
      unsubscribeChats();
      unloadNewMessageSound();
    };
  }, []);

  useImperativeHandle(chatRef, () => ({
    onShow: () => { onShow() },
    onHide: () => { onHide() },
  }));

  function onShow(): void {
    // reset the last read message count when showing the chat dialog
    lastReadMessageCountRef.current = chatsRef.current.length;
    visibleRef.current = true;
    setVisible(true);
  }

  function onHide(): void {
    visibleRef.current = false;
    messageTextHeightRef.current = 0;
    setVisible(false);
    setMessage(null);
  }

  function subscribeChats(): void {
    if (!syncChatsRef.current) {
      syncChatsRef.current = true;
    }
  }

  function unsubscribeChats(): void {
    if (syncChatsRef.current) {
      syncChatsRef.current = false;
    }
  }

  function loadChats(): void {
    if (!syncChatsRef.current) {
      return;
    }

    setMessagesSynced(true);
    ChatService.list(game.id, chatsRef.current ? chatsRef.current.length : 0).then((chats: Chat[]) => {
      if (chats && chats.length > chatsRef.current.length) {
        // play a sound for new messages
        if (chats.length > chatsRef.current.length) {
          playNewMessageSound();
        }

        if (!visibleRef.current) {
          // update the unread messages count
          const unreadNewMessagesCount = chats.length - lastReadMessageCountRef.current;
          footerRef.current.setUnreadMessagesCount(unreadNewMessagesCount);
        } else if (visibleRef.current) {
          lastReadMessageCountRef.current = chats.length;
        }

        chatsRef.current = chats;
        setChatMessages(createChatMessages());
      }
      loadChats();
    });
  }

  async function playNewMessageSound(): Promise<AVPlaybackStatus> {
    const { sound } = await Audio.Sound.createAsync(require('../../assets/sounds/new-message.mp3'));
    newMessageSoundRef.current = sound;
    return await sound.playAsync();
  }

  async function unloadNewMessageSound(): Promise<void> {
    if (newMessageSoundRef.current) {
      await newMessageSoundRef.current.unloadAsync();
    }
  }

  function createChatMessages(): ReactElement[] {
    const chatMessages: ReactElement[] = [];
    chatsRef.current.map((chat: Chat) => {
      chatMessages.push(createChatMessage(chat));
    });
    return chatMessages;
  }

  function createChatMessage(chat: Chat): ReactElement {
    return (
      <View
        key={'chatMessage_' + chat.id}
        style={styles.messageCard}>
        {
          viewingPlayer.userId !== chat.userId ?
            <View style={styles.topBar}>
              <View style={styles.userBar}>
                {getUserAvatar(chat.userId)}
                {getUsername(chat.userId, chat.username)}
              </View>
              {getMessageDate(chat.createdDate)}
            </View>
            :
            <View style={styles.topBar}>
              {getMessageDate(chat.createdDate)}
              <View style={styles.userBar}>
                {getUserAvatar(chat.userId)}
                {getUsername(chat.userId, chat.username)}
              </View>
            </View>
        }
        <Text
          variant='bodySmall'
          style={[styles.message, { textAlign: viewingPlayer.userId !== chat.userId ? 'left' : 'right' }]}>
          {chat.message}
        </Text>
      </View>
    );
  }

  function getUserAvatar(userId: number): ReactElement {
    if (!userAvatarMapRef.current.has(userId)) {
      userAvatarMapRef.current.set(userId, createUserAvatar(userId));

      // set a unique color for this user
      userColorRef.current.set(userId, colors[userAvatarMapRef.current.size - 1]);
    }
    return userAvatarMapRef.current.get(userId);
  }

  function createUserAvatar(userId: number): ReactElement {
    return (
      <Avatar.Image
        source={{ uri: process.env.EXPO_PUBLIC_PROFILE_PICTURE_URL + userId + '?' + new Date().getTime() }}
        size={24}
        style={styles.avatar} />
    );
  }

  function getUsername(userId: number, username: string): ReactElement {
    return (
      <Text
        variant='bodySmall'
        style={[styles.username, { color: userColorRef.current.get(userId) }]}>
        {username}
      </Text>
    );
  }

  function getMessageDate(createdDate: Date): ReactElement {
    return (
      <Text
        variant='bodySmall'
        style={styles.messageDate}>
        {'[' + moment(createdDate).format('HH:mm:ss') + ']'}
      </Text>
    );
  }

  function createSendMessagePanel(): ReactElement {
    return (
      <View style={styles.messageSend}>
        <TextInput
          textColor='#343a40'
          cursorColor='#343a40'
          placeholder={t('game.chat.message.placeholder')}
          mode='outlined'
          maxLength={250}
          multiline={true}
          style={[styles.messageText, { height: Math.max(60, messageTextHeightRef.current) }]}
          outlineStyle={styles.messageTextOutline}
          value={message}
          onContentSizeChange={(event) => {
            messageTextHeightRef.current = event.nativeEvent.contentSize.height
          }}
          onChangeText={(message) => setMessage(message)} />
        <IconButton
          icon="send-circle-outline"
          iconColor='#343a40'
          size={30}
          style={styles.sendButton}
          disabled={!message || sending}
          onPress={() => onPressSend()}
        />
      </View>
    );
  }

  function onPressSend() {
    if (!message) {
      return;
    }

    setSending(true);
    ChatService.save(game.id, message).then(() => {
      setSending(false);
      setMessage(null);
    }).catch((error) => {
      setSending(false);
      notificationRef.current.error(error.toString());
    });
  }

  return (
    <Modal
      visible={visible}
      onDismiss={() => onHide()}
      contentContainerStyle={styles.chatDialog}>
      {
        !messagesSynced ?
          <MessageBox message={t('game.chat.loading')} type='info' size={18} />
          :
          <View>
            {
              chatMessages ?
                <ScrollView
                  ref={view => (scrollViewRef.current = view)}
                  onContentSizeChange={() => scrollViewRef.current.scrollToEnd({ animated: true })}
                  style={styles.chat}>
                  {chatMessages}
                </ScrollView>
                :
                <MessageBox message={t('game.chat.no.message.found')} type='info' size={18} />
            }
            {createSendMessagePanel()}
          </View>
      }
    </Modal>
  )
};

const styles = StyleSheet.create({
  chatDialog: {
    backgroundColor: '#d8d8d8',
    borderColor: '#d8d8d8',
    borderWidth: 1,
    borderRadius: 7,
    margin: 20,
    padding: 20,
  },
  chat: {
    maxHeight: 320,
  },
  messageCard: {
    backgroundColor: '#fff',
    borderColor: '#fff',
    borderWidth: 0.5,
    borderRadius: 7,
    margin: 4,
    padding: 6,
  },
  topBar: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  userBar: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  avatar: {
    marginRight: 10,
  },
  username: {
    fontSize: 15,
    fontFamily: 'Gilroy-Bold',
  },
  message: {
    fontSize: 13,
    fontFamily: 'Gilroy-Regular',
    padding: 6,
  },
  messageDate: {
    fontSize: 13,
    fontFamily: 'Gilroy-RegularItalic',
  },
  messageSend: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 10,
  },
  messageText: {
    width: "84%",
    backgroundColor: '#fff',
    justifyContent: 'center',
  },
  messageTextOutline: {
    borderColor: "#fff",
    borderWidth: 1,
  },
  sendButton: {
    marginHorizontal: 0,
  },
});
