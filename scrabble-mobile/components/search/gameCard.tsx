import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import CountryFlag from 'react-native-country-flag';
import { Avatar, Card, Text, TouchableRipple } from 'react-native-paper';

import { PlayerList } from './playerList';

import ActionService from '../../services/action.service';

import { Action } from '../../model/action';

export function GameCard({ userId, game, notificationRef }) {

  const { t } = useTranslation();

  const [expanded, setExpanded] = useState<boolean>(false);
  const [lastAction, setLastAction] = useState<Action>();

  const versionRef = useRef<number>(0);
  const syncLastAction = useRef<boolean>(false);

  useEffect(() => {
    versionRef.current = game.version - 1;
    subscribeLastAction();
    getLastAction();

    return () => {
      unsubscribeLastAction();
    };
  }, []);

  function subscribeLastAction() {
    if (!syncLastAction.current) {
      syncLastAction.current = true;
    }
  }

  function unsubscribeLastAction() {
    if (syncLastAction.current) {
      syncLastAction.current = false;
    }
  }

  function getLastAction() {
    if (!syncLastAction.current) {
      return;
    }

    var currentVersion = versionRef.current + 1;
    ActionService.get(game.id, currentVersion).then((action: Action) => {
      if (action.version) {
        setLastAction(action);
        versionRef.current = action.version;

        if (action.gameStatus === 'IN_PROGRESS' || action.gameStatus === 'ENDED' || action.gameStatus === 'TERMINATED') {
          // TODO game is not in the waiting status anymore, remove the game from the search list
          versionRef.current = versionRef.current - 1;
          return;
        }
      }
      getLastAction();
    });
  }

  return (
    <Card style={styles.card}>
      <TouchableRipple onPress={() => setExpanded(!expanded)}>
        <View>
          <View style={styles.tag}>
            <CountryFlag
              isoCode={game.language === 'en' ? 'gb' : game.language}
              size={40} />
            <View>
              <Text
                variant="titleSmall"
                numberOfLines={1}
                style={styles.name}>
                {game.name}
              </Text>
              <View style={styles.properties}>
                <View style={styles.property}>
                  <Avatar.Icon
                    size={20}
                    icon="account-group"
                    style={styles.icon} />
                  <Text
                    variant="titleSmall"
                    style={styles.propertyLabel}>
                    {t('search.game.player.count', { 0: game.expectedPlayerCount })}
                  </Text>
                </View>
                <View style={styles.property}>
                  <Avatar.Icon
                    size={20}
                    icon="timer-sand"
                    style={styles.icon} />
                  <Text
                    variant="titleSmall"
                    style={styles.propertyLabel}>
                    {t('search.game.duration.minutes', { 0: game.duration / 60 })}
                  </Text>
                </View>
              </View>
            </View>
          </View>
          <View>
            {
              expanded ?
                <PlayerList userId={userId} game={game} lastAction={lastAction} notificationRef={notificationRef} />
                : ''
            }
          </View>
        </View>
      </TouchableRipple>
    </Card>
  )
};

const styles = StyleSheet.create({
  card: {
    marginBottom: 20,
  },
  tag: {
    flexDirection: 'row',
    backgroundColor: '#f7f3f9',
    borderColor: '#343a40',
    borderRadius: 4,
    borderWidth: 0.5,
    alignItems: 'center',
    padding: 14,
  },
  name: {
    marginLeft: 16,
  },
  properties: {
    flexDirection: 'row',
    marginTop: 4,
    marginLeft: 14,
  },
  icon: {
    backgroundColor: '#343a40',
  },
  property: {
    flexDirection: 'row',
    marginRight: 20,
  },
  propertyLabel: {
    marginLeft: 8,
  },
});
