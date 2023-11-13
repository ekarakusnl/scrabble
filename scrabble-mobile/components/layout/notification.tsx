import { ReactElement, useEffect, useImperativeHandle, useRef, useState } from 'react';
import { View } from 'react-native';
import { Snackbar } from 'react-native-paper';
import uuid from 'react-native-uuid';

interface Message {
  id: string,
  text: string,
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

  function error(text: string) {
    const message = {
      id: 'message_' + uuid.v4(),
      text: text
    };
    messagesRef.current.push(message);
  }

  function createSnackbar(): ReactElement {
    return (
      <Snackbar
        visible={currentMessageRef.current.text != null}
        duration={MESSAGE_DURATION}
        onDismiss={() => { }}>
        {currentMessageRef.current.text}
      </Snackbar>
    );
  }

  return (
    <View>
      {snackbar}
    </View>
  )
};
