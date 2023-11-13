import { router } from 'expo-router';
import { useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { Avatar, Button, HelperText, PaperProvider, Text, TextInput, TouchableRipple } from 'react-native-paper';

import { Notification } from '../components/layout/notification';
import { Splash } from '../components/layout/splash';

import AuthenticationService from '../services/authentication.service';

import { UserToken } from '../model/user-token';

import '../translations/i18n';

export default function LoginScreen() {

  const { t, i18n } = useTranslation();

  const [username, setUsername] = useState<string>();
  const [password, setPassword] = useState<string>();
  const [hasUsername, setHasUsername] = useState<boolean>(false);
  const [hasPassword, setHasPassword] = useState<boolean>(false);

  const notificationRef = useRef(null);

  function onPressLogin(): void {
    if (!username || !password) {
      setHasUsername(true);
      setHasPassword(true);
      return;
    }

    AuthenticationService.login(username, password).then((userToken: UserToken) => {
      i18n.changeLanguage(userToken.preferredLanguage ? userToken.preferredLanguage : 'en');
      router.push('/menu');
    }).catch((error) => {
      notificationRef.current.error(error.toString());
    });
  }

  function onPressForgotPassword(): void {
    // Do something about forgot password operation
  }

  function onPressSignup(): void {
    router.push('/signup');
  }

  return (
    <PaperProvider>
      <View style={styles.container}>
        <Splash page='signin' />
        <View style={styles.body}>
          <View style={styles.credentials}>
            <View style={styles.label}>
              <Avatar.Icon
                size={24}
                icon="account-outline"
                style={styles.labelIcon} />
              <Text
                variant="bodyLarge"
                style={styles.labelText}>
                {t('login.user.username')}
              </Text>
            </View>
            <TextInput
              placeholder={t('login.user.username.placeholder')}
              style={styles.inputText}
              outlineStyle={styles.inputTextOutline}
              cursorColor='#007bff'
              mode='outlined'
              onChangeText={(username) => setUsername(username)}
              onPressIn={() => setHasUsername(true)}
              value={username} />
            <HelperText type="error" visible={!username && hasUsername}>
              {t('validation.required', { 0: t('login.user.username') })}
            </HelperText>
          </View>
          <View style={styles.credentials}>
            <View style={styles.label}>
              <Avatar.Icon
                size={24}
                icon="lock-outline"
                style={styles.labelIcon} />
              <Text
                variant="bodyLarge"
                style={styles.labelText}>
                {t('login.user.password')}
              </Text>
            </View>
            <TextInput
              placeholder={t('login.user.password.placeholder')}
              style={styles.inputText}
              outlineStyle={styles.inputTextOutline}
              cursorColor='#007bff'
              mode='outlined'
              secureTextEntry={true}
              onChangeText={(password) => setPassword(password)}
              onPressIn={() => setHasPassword(true)}
              value={password}
            />
            <HelperText type="error" visible={!password && hasPassword}>
              {t('validation.required', { 0: t('login.user.password') })}
            </HelperText>
          </View>
        </View>
        <View style={styles.footer}>
          <Button
            mode="contained"
            style={styles.loginButton}
            onPress={onPressLogin}>
            {t('login.button.signin')}
          </Button>
          <TouchableRipple onPress={onPressForgotPassword}>
            <Text
              variant='bodyMedium'
              style={styles.forgetPasswordText}>
              {t('login.link.forgot.password')}
            </Text>
          </TouchableRipple>
          <View style={styles.signup}>
            <Text variant='bodyMedium'>{t('login.no.account')}</Text>
            <TouchableRipple onPress={onPressSignup}>
              <Text
                variant='bodyMedium'
                style={styles.signupText}>
                {t('login.link.signup')}
              </Text>
            </TouchableRipple>
          </View>
        </View>
        <Notification notificationRef={notificationRef} />
      </View>
    </PaperProvider>
  )
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  body: {
    alignItems: 'center',
  },
  credentials: {
    width: "100%",
    alignItems: 'center',
    marginBottom: 20,
  },
  label: {
    flexDirection: 'row',
  },
  labelIcon: {
    backgroundColor: "#007bff",
  },
  labelText: {
    marginLeft: 6,
  },
  inputText: {
    width: "70%",
    height: 34,
    backgroundColor: '#fff',
    borderTopWidth: 0,
    borderLeftWidth: 0,
    borderRightWidth: 0,
    textAlign: "center",
  },
  inputTextOutline: {
    borderColor: "#007bff",
    borderTopWidth: 0,
    borderLeftWidth: 0,
    borderRightWidth: 0,
    borderWidth: 1,
  },
  loginButton: {
    width: "60%",
    backgroundColor: "#007bff",
  },
  forgetPasswordText: {
    color: "#007bff",
    textDecorationLine: 'underline',
    marginTop: 10
  },
  signup: {
    flexDirection: 'row',
    marginTop: 50,
    marginBottom: 30,
  },
  signupText: {
    color: "#007bff",
    textDecorationLine: 'underline',
    marginLeft: 4
  },
  footer: {
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 20,
  },
});
