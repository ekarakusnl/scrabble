import 'dotenv/config';

export default ({ config }) => ({
    
  expo: {
    ...config,
    name: 'scrabble-mobile',
    slug: 'scrabble-mobile',
    version: '1.0.0',
    scheme: 'mobile-scheme',
    orientation: 'portrait',
    icon: './assets/images/icon.png',
    userInterfaceStyle: 'light',
    splash: {
      image: './assets/images/splash.png',
      resizeMode: 'contain',
      backgroundColor: '#ffffff',
    },
    assetBundlePatterns: ['**/*'],
    ios: {
      supportsTablet: true,
    },
    android: {
      adaptiveIcon: {
        foregroundImage: './assets/images/adaptive-icon.png',
        backgroundColor: '#ffffff',
      },
      package: 'net.olivialabs.scrabble',
    },
    web: {
      favicon: './assets/images/favicon.png',
      bundler: 'metro',
    },
    plugins: ['expo-router'],
    // Environment variables injected at build time
    extra: {
      EXPO_PUBLIC_GATEWAY_URL: process.env.EXPO_PUBLIC_GATEWAY_URL || 'http://localhost:6080',
      EXPO_PUBLIC_PROFILE_PICTURE_URL: process.env.EXPO_PUBLIC_PROFILE_PICTURE_URL || 'https://www.google.com/',

    },
    "owner": "ekarakus",
  },
});