import * as Font from 'expo-font';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ActivityIndicator, AppRegistry, Platform } from 'react-native';
import { MD3LightTheme, PaperProvider, configureFonts } from 'react-native-paper';

import Login from './login';
import Menu from './menu';

import AxiosInterceptor from '../utilities/axios.interceptor';
import StorageService from '../services/storage.service';

import '../translations/i18n';

const fontConfig = {
  displayLarge: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  displayMedium: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  displaySmall: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  headlineLarge: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  headlineMedium: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  headlineSmall: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  titleLarge: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  titleMedium: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  titleSmall: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  labelLarge: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  labelMedium: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  labelSmall: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  bodyLarge: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  bodyMedium: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
  bodySmall: {
    fontFamily: Platform.select({
      web: 'Gilroy-Regular',
      ios: 'Gilroy-Regular',
      android: 'Gilroy-Regular',
      default: 'Gilroy-Regular',
    }),
  },
} as const;

const theme = {
  ...MD3LightTheme,
  fonts: configureFonts({ config: fontConfig }),
};

export default function Index() {

  const { t, i18n } = useTranslation();

  const [token, setToken] = useState<string>();
  const [fontLoaded, setFontLoaded] = useState<boolean>(false);
  const [tokenChecked, setTokenChecked] = useState<boolean>(false);
  const [preferredLanguageChecked, setPreferredLanguageChecked] = useState<boolean>(false);

  AxiosInterceptor.createRequestInterceptor();
  AxiosInterceptor.createResponseInterceptor(t);

  useEffect(() => {
    async function loadFont(): Promise<void> {
      await Font.loadAsync({
        'Gilroy-Light': require('../assets/fonts/Gilroy-Light.ttf'),
        'Gilroy-Regular': require('../assets/fonts/Gilroy-Regular.ttf'),
        'Gilroy-RegularItalic': require('../assets/fonts/Gilroy-RegularItalic.ttf'),
        'Gilroy-Bold': require('../assets/fonts/Gilroy-Bold.ttf'),
        'AbhayaLibre-Regular': require('../assets/fonts/AbhayaLibre-Regular.ttf'),
        'AbhayaLibre-Medium': require('../assets/fonts/AbhayaLibre-Medium.ttf'),
        'AbhayaLibre-SemiBold': require('../assets/fonts/AbhayaLibre-SemiBold.ttf'),
        'AbhayaLibre-Bold': require('../assets/fonts/AbhayaLibre-Bold.ttf'),
        'SpaceMono-Regular': require('../assets/fonts/SpaceMono-Regular.ttf'),
        'SpaceMono-Bold': require('../assets/fonts/SpaceMono-Bold.ttf'),
        'Playball-Regular': require('../assets/fonts/Playball-Regular.ttf'),
      });

      setFontLoaded(true);
    }

    async function loadToken(): Promise<void> {
      setToken(await StorageService.getToken());
      setTokenChecked(true);
    }

    async function loadPreferredLanguage(): Promise<void> {
      const preferredLanguage = await StorageService.getPreferredLanguage();
      i18n.changeLanguage(preferredLanguage ? preferredLanguage : 'en').then(() => {
        setPreferredLanguageChecked(true);
      });
    }

    loadFont();
    loadToken();
    loadPreferredLanguage();

    return () => {
      setTokenChecked(false);
      setPreferredLanguageChecked(false);
    };
  }, []);

  if (!fontLoaded || !tokenChecked || !preferredLanguageChecked) {
    return <ActivityIndicator animating={true} />;
  }

  return (
    <PaperProvider theme={theme}>
      {token ? <Menu /> : <Login />}
    </PaperProvider>
  );
}

AppRegistry.registerComponent('scrabble-mobile', () => Index);
