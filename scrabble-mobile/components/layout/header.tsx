import { router } from 'expo-router';
import { ReactElement, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { Appbar, Avatar, Menu, useTheme, } from 'react-native-paper';

import AuthenticationService from '../../services/authentication.service';
import StorageService from '../../services/storage.service';

export function Header({ title, previousScreen }) {

  const { t } = useTranslation();
  const theme = useTheme();

  const [header, setHeader] = useState<ReactElement>();
  const userIdRef = useRef<number>();
  const showMenuRef = useRef<boolean>(false);
  const avatarActionRef = useRef<ReactElement>();
  const profilePictureURIRef = useRef<string>();

  useEffect(() => {
    if (!userIdRef.current) {
      StorageService.getUserId().then(userId => {
        userIdRef.current = Number(userId);
        profilePictureURIRef.current = process.env.EXPO_PUBLIC_PROFILE_PICTURE_URL + userIdRef.current + '?' + new Date().getTime();
        avatarActionRef.current = createAvatarAction();
        setHeader(createHeader());
      });
    }

    return () => {
    };
  }, []);

  function createHeader(): ReactElement {
    return (
      <Appbar.Header
        style={{ backgroundColor: theme.colors.elevation.level2 }}
        mode='small'
        statusBarHeight={0}>
        {
          previousScreen ?
            <Appbar.BackAction onPress={() => router.push(previousScreen)} />
            : ''
        }
        <Appbar.Content
          title={title}
          titleStyle={styles.headerTitle} />
        <Menu
          visible={showMenuRef.current}
          onDismiss={() => onPressMenu(false)}
          anchor={avatarActionRef.current}>
          <Menu.Item
            title={t('user.menu.account')}
            onPress={() => onPressAccount()} />
          <Menu.Item
            title={t('user.menu.signout')}
            onPress={() => onPressLogout()} />
        </Menu>
      </Appbar.Header >
    )
  }

  function createAvatarAction(): ReactElement {
    if (!userIdRef.current) {
      return null;
    }

    return (
      <Appbar.Action
        icon={() => <Avatar.Image source={{ uri: profilePictureURIRef.current }} size={30} />}
        onPress={() => onPressMenu(true)} />
    );
  }

  function onPressLogout(): void {
    AuthenticationService.logout();
    router.push("/login");
  };

  function onPressAccount(): void {
    router.push("/account");
  };

  function onPressMenu(showMenu: boolean): void {
    showMenuRef.current = showMenu;
    setHeader(createHeader());
  };

  return (
    <View>
      {header}
    </View>
  )
};

const styles = StyleSheet.create({
  headerTitle: {
    fontFamily: 'Playball-Regular',
  }
});
