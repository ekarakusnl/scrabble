import { ReactElement, useEffect, useImperativeHandle, useRef, useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { Avatar, Snackbar, Text } from 'react-native-paper';
import uuid from 'react-native-uuid';

interface Message {
  id: string,
  text: string,
  props: MessageProps,
};

interface MessageProps {
  messageType: MessageType,
  iconClass: string,
  backgroundColor: string,
  textColor: string,
};

enum MessageType {
  error, success, info, warning
};

const MESSAGE_LISTENER_INTERVAL = 500;
const MESSAGE_DURATION = 3000;

export function Notification({ notificationRef }) {

  const [snackbar, setSnackbar] = useState<ReactElement>();

  const currentMessageRef = useRef<Message>();
  const messagesRef = useRef<Message[]>([]);
  const notificationMessageActiveRef = useRef<boolean>(false);
  const notificationMessageIntervalRef = useRef<NodeJS.Timeout>();

  useEffect(() => {
    startListener();
    activateListener();

    return () => {
      stopListener();
    };
  }, []);

  useImperativeHandle(notificationRef, () => ({
    error: (message: string) => { error(message) },
    success: (message: string) => { success(message) },
    info: (message: string) => { info(message) },
    warning: (message: string) => { warning(message) },
  }));

  function startListener(): void {
    if (notificationMessageIntervalRef.current) {
      return;
    }

    notificationMessageIntervalRef.current = setInterval(() => {
      if (!notificationMessageActiveRef.current) {
        return;
      }
      if (!messagesRef.current || messagesRef.current.length === 0) {
        if (!currentMessageRef.current) {
          currentMessageRef.current = null;
          setSnackbar(null);
        }
      } else if (!currentMessageRef.current) {
        // set the current message
        currentMessageRef.current = messagesRef.current[0];
        // show the current message
        setSnackbar(createSnackbar());
        // set the timeout so the message could disappear after the given time
        setTimeout(dismiss, MESSAGE_DURATION, currentMessageRef.current.id);
      }
    }, MESSAGE_LISTENER_INTERVAL);
  }

  function activateListener(): void {
    if (!notificationMessageActiveRef.current) {
      notificationMessageActiveRef.current = true;
    }
  }

  function stopListener(): void {
    if (notificationMessageActiveRef.current) {
      notificationMessageActiveRef.current = false;
      clearInterval(notificationMessageIntervalRef.current);
      notificationMessageIntervalRef.current = null;
    }
  }

  function dismiss(id: string): void {
    // remove the expired message
    messagesRef.current = messagesRef.current.filter(message => message.id !== id);
    // reset the current message so a new message could be showed
    currentMessageRef.current = null;
  }

  function addQueueMessage(text: string, props: MessageProps,) {
    const message = {
      id: 'message_' + uuid.v4(),
      text: text,
      props: props,
    };
    messagesRef.current.push(message);
  }

  function error(text: string) {
    const messageProps: MessageProps = {
      messageType: MessageType.error,
      iconClass: 'minus-circle-outline',
      backgroundColor: '#dc3545',
      textColor: '#fff'
    };
    addQueueMessage(text, messageProps);
  }

  function success(text: string) {
    const messageProps: MessageProps = {
      messageType: MessageType.success,
      iconClass: 'check-outline',
      backgroundColor: '#28a745',
      textColor: '#fff'
    };
    addQueueMessage(text, messageProps);
  }

  function info(text: string) {
    const messageProps: MessageProps = {
      messageType: MessageType.info,
      iconClass: 'information-outline',
      backgroundColor: '#17a2b8',
      textColor: '#fff'
    };
    addQueueMessage(text, messageProps);
  }

  function warning(text: string) {
    const messageProps: MessageProps = {
      messageType: MessageType.warning,
      iconClass: 'alert-outline',
      backgroundColor: '#ffc107',
      textColor: '#000'
    };
    addQueueMessage(text, messageProps);
  }

  function createSnackbar(): ReactElement {
    const messageProps = currentMessageRef.current.props;
    const messageIcon = (
      <Avatar.Icon
        size={36}
        icon={messageProps.iconClass}
        style={{ backgroundColor: messageProps.backgroundColor }} />
    );
    return (
      <Snackbar
        visible={currentMessageRef.current.text != null}
        duration={MESSAGE_DURATION}
        onDismiss={() => { }}
        style={[styles.snackbar, { backgroundColor: messageProps.backgroundColor }]}>
        <View style={styles.messagePanel}>
          {messageIcon}
          <Text style={[styles.message, { color: messageProps.textColor }]}>
            {currentMessageRef.current.text}
          </Text>
        </View>
      </Snackbar>
    );
  }

  return (
    <View>
      {snackbar}
    </View>
  )
};

const styles = StyleSheet.create({
  snackbar: {
    backgroundColor: '#f7f3f9',
    borderColor: '#343a40',
    borderRadius: 4,
    borderWidth: 1,
    alignItems: 'center',
  },
  messagePanel: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'center',
  },
  message: {
    fontFamily: 'Gilroy-Regular',
    flexShrink: 1,
    marginLeft: 4,
  },
});
