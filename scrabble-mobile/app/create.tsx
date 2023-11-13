import { router } from 'expo-router';
import { useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { Avatar, Button, Chip, HelperText, PaperProvider, Text, TextInput } from 'react-native-paper';

import { Header } from '../components/layout/header';
import { Notification } from '../components/layout/notification';

import GameService from '../services/game.service';

import { Game } from '../model/game';

interface Language {
  name: string,
  code: string,
};

const playerCounts: number[] = [1, 2, 3, 4];
const durations: number[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

export default function CreateScreen() {

  const { t } = useTranslation();

  const [name, setName] = useState<string>();
  const [playerCount, setPlayerCount] = useState<number>(2);
  const [language, setLanguage] = useState<string>('en');
  const [duration, setDuration] = useState<number>(3);
  const [hasName, setHasName] = useState<boolean>();

  const notificationRef = useRef(null);

  const languages: Language[] = [
    { name: t('language.en'), code: 'en', },
    { name: t('language.fr'), code: 'fr', },
    { name: t('language.de'), code: 'de', },
    { name: t('language.nl'), code: 'nl', },
    { name: t('language.tr'), code: 'tr', },
  ];

  function isPlayerCountSelected(selectedPlayerCount: number): boolean {
    return playerCount === selectedPlayerCount;
  }

  function isLanguageSelected(selectedLanguage: string): boolean {
    return language === selectedLanguage;
  }

  function isDurationSelected(selectedDuration: number): boolean {
    return duration === selectedDuration;
  }

  function onPressCreate(): void {
    if (!name) {
      setHasName(true);
      return;
    }

    GameService.create(name, playerCount, language, duration * 60).then((game: Game) => {
      router.push({ pathname: '/game', params: { id: game.id } });
    }).catch((error) => {
      notificationRef.current.error(error.toString());
    });
  }

  return (
    <PaperProvider>
      <View style={styles.container}>
        <Header title={t('game.create.new')} previousScreen='menu' />
        <View style={styles.body}>
          <View style={styles.label}>
            <Avatar.Icon
              size={24}
              icon="label-outline"
              style={styles.labelIcon} />
            <Text
              variant="bodyLarge"
              style={styles.labelText}>
              {t('game.create.name')}
            </Text>
          </View>
          <TextInput
            style={styles.inputText}
            outlineStyle={styles.inputTextOutline}
            cursorColor='#007bff'
            placeholder={t('game.create.name.placeholder')}
            mode='outlined'
            maxLength={30}
            onChangeText={(name) => setName(name)}
            onPressIn={() => setHasName(true)}
            value={name} />
          {!name && hasName ?
            <HelperText
              type="error"
              visible={!name && hasName}
              style={styles.validationError}>
              {t('validation.required', { 0: t('game.create.name') })}
            </HelperText>
            : ''
          }
          <View style={styles.label}>
            <Avatar.Icon
              size={24}
              icon="account-group-outline"
              style={styles.labelIcon} />
            <Text
              variant="bodyLarge"
              style={styles.labelText}>
              {t('game.create.player.count')}
            </Text>
          </View>
          <View style={styles.chipView}>
            {
              playerCounts.map((playerCount: number) => (
                <Chip
                  key={'player_' + playerCount}
                  style={styles.chip}
                  textStyle={styles.chipText}
                  selectedColor='#007bff'
                  onPress={() => setPlayerCount(playerCount)}
                  selected={isPlayerCountSelected(playerCount)}>
                  {playerCount}
                </Chip>
              ))
            }
          </View>
          <View style={styles.label}>
            <Avatar.Icon
              size={24}
              icon="flag-outline"
              style={styles.labelIcon} />
            <Text
              variant="bodyLarge"
              style={styles.labelText}>
              {t('game.create.language')}
            </Text>
          </View>
          <View style={styles.chipView}>
            {
              languages.map((language: Language) => (
                <Chip
                  key={'language_' + language.code}
                  style={styles.chip}
                  textStyle={styles.chipText}
                  selectedColor='#007bff'
                  onPress={() => setLanguage(language.code)}
                  selected={isLanguageSelected(language.code)}>
                  {language.name}
                </Chip>
              ))
            }
          </View>
          <View style={styles.label}>
            <Avatar.Icon
              size={24}
              icon="timer-sand"
              style={styles.labelIcon} />
            <Text
              variant="bodyLarge"
              style={styles.labelText}>
              {t('game.create.duration')}
            </Text>
          </View>
          <View style={styles.chipView}>
            {
              durations.map((duration: number) => (
                <Chip
                  key={'duration_' + duration}
                  style={styles.chip}
                  textStyle={styles.chipText}
                  selectedColor='#007bff'
                  onPress={() => setDuration(duration)}
                  selected={isDurationSelected(duration)}>
                  {duration}
                </Chip>
              ))
            }
          </View>
        </View>
        <View style={styles.footer}>
          <Button
            mode="contained"
            style={styles.createButton}
            textColor='#fff'
            labelStyle={{ fontFamily: 'Playball-Regular', fontSize: 20 }}
            onPress={onPressCreate}>
            {t('game.create.button.create')}
          </Button>
        </View>
        <Notification notificationRef={notificationRef} />
      </View>
    </PaperProvider>
  )
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  body: {
    alignItems: 'center',
  },
  label: {
    flexDirection: 'row',
    marginTop: 50,
    marginBottom: 6,
  },
  labelIcon: {
    backgroundColor: "#007bff",
  },
  labelText: {
    marginLeft: 6,
  },
  inputText: {
    width: "66%",
    height: 34,
    backgroundColor: '#fff',
    textAlign: "center",
  },
  inputTextOutline: {
    borderColor: "#007bff",
    borderTopWidth: 0,
    borderLeftWidth: 0,
    borderRightWidth: 0,
    borderBottomWidth: 1,
  },
  validationError: {
    fontSize: 13,
    fontFamily: 'Gilroy-RegularItalic',
  },
  chipView: {
    width: '80%',
    justifyContent: 'center',
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  chip: {
    backgroundColor: "#fff",
    borderColor: "#007bff",
    borderWidth: 1,
    margin: 4,
  },
  chipText: {
    color: '#007bff',
  },
  createButton: {
    backgroundColor: "#007bff",
  },
  footer: {
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 70,
  },
});
