import { useEffect, useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { Avatar, Text } from 'react-native-paper';

export function MessageBox({ message, type, size }) {

  const [icon, setIcon] = useState<string>();
  const [iconBackgroundColor, setIconBackgroundColor] = useState<string>();

  useEffect(() => {
    if (!message || !type) {
      return;
    }

    switch (type) {
      case 'error': {
        setIcon('minus-circle-outline');
        setIconBackgroundColor('#dc3545');
        break;
      }
      case 'warning': {
        setIcon('exclamation');
        setIconBackgroundColor('#ffc107');
        break;
      }
      case 'info': {
        setIcon('information-outline');
        setIconBackgroundColor('#17a2b8');
        break;
      }
      case 'success': {
        setIcon('check-outline');
        setIconBackgroundColor('#28a745');
        break;
      }
      default: {
        setIcon('information-outline');
        setIconBackgroundColor('#17a2b8');
        break;
      }
    }

    return () => {
    };
  }, []);

  return (
    <View style={styles.messageBox}>
      {
        icon ?
          <Avatar.Icon
            size={48}
            icon={icon}
            style={{ backgroundColor: iconBackgroundColor }} />
          : ''
      }
      <Text
        variant="titleLarge"
        style={[styles.message, { fontSize: size }]}>
        {message}
      </Text>
    </View>
  )
};

const styles = StyleSheet.create({
  messageBox: {
    flexDirection: 'row',
    backgroundColor: '#f7f3f9',
    borderColor: '#343a40',
    borderRadius: 4,
    borderWidth: 0.5,
    alignItems: 'center',
    padding: 14,
  },
  message: {
    flexShrink: 1,
    marginLeft: 16
  },
});
