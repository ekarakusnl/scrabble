import { AVPlaybackStatus, Audio } from 'expo-av';
import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { Avatar, Text } from 'react-native-paper';

export function RemainingTime({ totalDurationInSeconds, lastUpdatedDate }) {

  const { t } = useTranslation();

  const [remainingTime, setRemainingTime] = useState<string>();

  const remainingTimeRef = useRef<string>();
  const remainingTimeActiveRef = useRef<boolean>(false);
  const remainingTimeIntervalRef = useRef<any>();
  const remainingTimeInSecondsRef = useRef<number>();
  const timerSoundRef = useRef<Audio.Sound>();
  const timerCountdownRef = useRef<boolean>();

  useEffect(() => {
    startTimer();

    if (!totalDurationInSeconds || !lastUpdatedDate) {
      setRemainingTime('00:00');
      return;
    }

    activateTimer();
    loadTimerSound();

    return () => {
      stopTimer();
      stopTimerSound();
      unloadTimerSound();
    };
  }, [lastUpdatedDate]);

  function startTimer(): void {
    if (remainingTimeIntervalRef.current) {
      return;
    }

    remainingTimeIntervalRef.current = setInterval(() => {
      if (!remainingTimeActiveRef.current) {
        return;
      }

      const passedDurationInSeconds = (new Date().getTime() - new Date(lastUpdatedDate).getTime()) / 1000;
      remainingTimeInSecondsRef.current = totalDurationInSeconds - Math.round(passedDurationInSeconds);
      if (remainingTimeInSecondsRef.current < 0) {
        remainingTimeInSecondsRef.current = 0;
        remainingTimeRef.current = '00:00';
        return;
      }

      const remainingSeconds = remainingTimeInSecondsRef.current % 60;
      const remainingMinutes = (remainingTimeInSecondsRef.current - remainingSeconds) / 60;

      const remainingSecondsString = (remainingSeconds < 10 ? '0' : '') + remainingSeconds.toString();
      const remainingMinutesString = (remainingMinutes < 10 ? '0' : '') + remainingMinutes.toString();

      remainingTimeRef.current = remainingMinutesString + ':' + remainingSecondsString;
      if (remainingMinutes === 0 && remainingSeconds <= 15 && !timerCountdownRef.current) {
        playTimerSound();
      }
      setRemainingTime(remainingTimeRef.current);
    }, 1000);
  }

  function activateTimer(): void {
    if (!remainingTimeActiveRef.current) {
      remainingTimeActiveRef.current = true;
    }
  }

  function stopTimer(): void {
    if (remainingTimeActiveRef.current) {
      remainingTimeActiveRef.current = false;
      remainingTimeRef.current = null;
      clearInterval(remainingTimeIntervalRef.current);
      remainingTimeIntervalRef.current = null;
    }
  }

  async function loadTimerSound(): Promise<void> {
    const { sound } = await Audio.Sound.createAsync(require('../../../assets/sounds/timer-15-seconds.mp3'));
    timerSoundRef.current = sound;
  }

  async function playTimerSound(): Promise<AVPlaybackStatus> {
    timerCountdownRef.current = true;
    return await timerSoundRef.current.playAsync();
  }

  async function stopTimerSound(): Promise<AVPlaybackStatus> {
    if (timerSoundRef.current) {
      timerCountdownRef.current = false;
      return await timerSoundRef.current.stopAsync();
    }
  }

  async function unloadTimerSound(): Promise<void> {
    if (timerSoundRef.current) {
      await timerSoundRef.current.unloadAsync();
    }
  }

  return (
    <View style={styles.remainingTime}>
      <Avatar.Icon
        size={48}
        style={styles.durationTimerIcon}
        icon="timer-settings-outline" />
      <Text variant="bodySmall" style={styles.remainingTimeText}>{remainingTime}</Text>
      <Text variant="bodySmall" style={styles.remainingTimeLabel}>{t('game.scoreboard.time')}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  remainingTime: {
    backgroundColor: '#dc3545',
    borderColor: '#dc3545',
    borderWidth: 0.5,
    borderRadius: 7,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 8,
  },
  durationTimerIcon: {
    backgroundColor: '#dc3545',
  },
  remainingTimeText: {
    color: '#fff',
    fontFamily: 'SpaceMono-Regular',
    fontSize: 14,
    paddingLeft: 4,
    paddingRight: 4,
    paddingBottom: 2,
  },
  remainingTimeLabel: {
    color: '#fff',
    fontFamily: 'Playball-Regular',
    fontSize: 15,
    marginTop: 4,
  },
});
