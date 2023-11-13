import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { Avatar, Text } from 'react-native-paper';

export function PassedTime({ createdDate }) {

  const { t } = useTranslation();

  const [passedTime, setPassedTime] = useState<string>();
  const passedTimeRef = useRef<string>();
  const passedTimeActiveRef = useRef<boolean>(false);
  const passedTimeIntervalRef = useRef<any>();
  const passedTimeInSecondsRef = useRef<number>();

  useEffect(() => {
    startTimer();

    if (!createdDate) {
      setPassedTime('00:00');
      return;
    }

    activateTimer();

    return () => {
      stopTimer();
    };
  }, []);

  function startTimer(): void {
    if (passedTimeIntervalRef.current) {
      return;
    }

    passedTimeIntervalRef.current = setInterval(() => {
      if (!passedTimeActiveRef.current) {
        return;
      }

      passedTimeInSecondsRef.current = Math.trunc(passedTimeInSecondsRef.current + 1);

      const passedSeconds = passedTimeInSecondsRef.current % 60;
      const passedMinutes = (passedTimeInSecondsRef.current - passedSeconds) / 60;

      const passedSecondsString = (passedSeconds < 10 ? '0' : '') + passedSeconds.toString();
      const passedMinutesString = (passedMinutes < 10 ? '0' : '') + passedMinutes.toString();

      passedTimeRef.current = (passedMinutesString + ':' + passedSecondsString);
      setPassedTime(passedTimeRef.current);
    }, 1000);
  }

  function activateTimer(): void {
    if (!passedTimeActiveRef.current) {
      const passedTimeInSeconds = (new Date().getTime() - new Date(createdDate).getTime()) / 1000;
      passedTimeInSecondsRef.current = passedTimeInSeconds;
      passedTimeActiveRef.current = true;
    }
  }

  function stopTimer(): void {
    if (passedTimeActiveRef.current) {
      passedTimeActiveRef.current = false;
      passedTimeRef.current = null;
      clearInterval(passedTimeIntervalRef.current);
      passedTimeIntervalRef.current = null;
    }
  }

  return (
    <View style={styles.passedTime}>
      <Avatar.Icon
        size={48}
        style={styles.passedTimerIcon}
        icon="timer-settings-outline" />
      <Text variant="bodySmall" style={styles.passedTimeText}>{passedTime}</Text>
      <Text variant="bodySmall" style={styles.passedTimeLabel}>{t('game.scoreboard.time')}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  passedTime: {
    backgroundColor: '#ffc107',
    borderColor: '#ffc107',
    borderWidth: 0.5,
    borderRadius: 7,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 8,
  },
  passedTimerIcon: {
    backgroundColor: '#ffc107',
  },
  passedTimeText: {
    color: '#000',
    fontFamily: 'SpaceMono-Regular',
    fontSize: 14,
    paddingLeft: 4,
    paddingRight: 4,
    paddingBottom: 2,
  },
  passedTimeLabel: {
    color: '#000',
    fontFamily: 'Playball-Regular',
    fontSize: 15,
    marginTop: 4,
  },
});
