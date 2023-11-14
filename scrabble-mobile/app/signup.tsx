import { router } from 'expo-router';
import { useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { Avatar, Button, HelperText, PaperProvider, Text, TextInput, TouchableRipple } from 'react-native-paper';

import { Notification } from '../components/layout/notification';
import { Splash } from '../components/layout/splash';

import UserService from '../services/user.service';

const EMAIL_REGEX = new RegExp("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

export default function SignupScreen() {

  const { t } = useTranslation();

  const [username, setUsername] = useState<string>();
  const [email, setEmail] = useState<string>();
  const [password, setPassword] = useState<string>();
  const [hasUsername, setHasUsername] = useState<boolean>();
  const [hasEmail, setHasEmail] = useState<boolean>();
  const [emailValidationMessage, setEmailValidationMessage] = useState<string>();
  const [hasValidEmail, setHasValidEmail] = useState<boolean>();
  const [hasPassword, setHasPassword] = useState<boolean>();

  const notificationRef = useRef(null);

  function onChangeEmail(email: string): void {
    if (!email) {
      setHasValidEmail(false);
      setEmailValidationMessage(t('validation.required', { 0: t('signup.user.email') }));
      return;
    }

    if (!EMAIL_REGEX.test(email)) {
      setHasValidEmail(false);
      setEmailValidationMessage(t('validation.not.valid', { 0: t('signup.user.email') }));
      return;
    }

    setEmail(email);
    setHasValidEmail(true);
    setEmailValidationMessage(null);
  }

  function onPressSignup(): void {
    if (!username || !hasValidEmail || !password) {
      setHasUsername(true);
      setHasEmail(true);
      onChangeEmail(email);
      setHasPassword(true);
      return;
    }

    UserService.create(username, email, password).then(() => {
      router.push('/login');
    }).catch((error) => {
      notificationRef.current.error(error.toString());
    });
  }

  function onPressSignin(): void {
    router.push('/login');
  }

  return (
    <PaperProvider>
      <View style={styles.container}>
        <Splash page='signup' />
        <View style={styles.body}>
          <View style={styles.userInput}>
            <View style={styles.label}>
              <Avatar.Icon
                size={24}
                icon="account-outline"
                style={styles.labelIcon} />
              <Text
                variant="bodyLarge"
                style={styles.labelText}>
                {t('signup.user.username')}
              </Text>
            </View>
            <TextInput
              style={styles.inputText}
              outlineStyle={styles.inputTextOutline}
              cursorColor='#007bff'
              placeholder={t('signup.user.username.placeholder')}
              mode='outlined'
              maxLength={30}
              onChangeText={(username) => setUsername(username)}
              onPressIn={() => setHasUsername(true)}
              value={username} />
            {!username && hasUsername ?
              <HelperText
                type="error"
                visible={!username && hasUsername}
                style={styles.validationError}>
                {t('validation.required', { 0: t('signup.user.username') })}
              </HelperText>
              : ''
            }
          </View>
          <View style={styles.userInput}>
            <View style={styles.label}>
              <Avatar.Icon
                size={24}
                icon="at"
                style={styles.labelIcon} />
              <Text
                variant="bodyLarge"
                style={styles.labelText}>
                {t('signup.user.email')}
              </Text>
            </View>
            <TextInput
              style={styles.inputText}
              outlineStyle={styles.inputTextOutline}
              cursorColor='#007bff'
              placeholder={t('signup.user.email.placeholder')}
              mode='outlined'
              maxLength={30}
              onChangeText={(email) => onChangeEmail(email)}
              onPressIn={() => setHasEmail(true)}
              value={email} />
            {!hasValidEmail && hasEmail ?
              <HelperText
                type="error"
                visible={!email && hasEmail}
                style={styles.validationError}>
                {emailValidationMessage}
              </HelperText>
              : ''
            }
          </View>
          <View style={styles.userInput}>
            <View style={styles.label}>
              <Avatar.Icon
                size={24}
                icon="lock-outline"
                style={styles.labelIcon} />
              <Text
                variant="bodyLarge"
                style={styles.labelText}>
                {t('signup.user.password')}
              </Text>
            </View>
            <TextInput
              style={styles.inputText}
              outlineStyle={styles.inputTextOutline}
              cursorColor='#007bff'
              placeholder={t('signup.user.password.placeholder')}
              mode='outlined'
              secureTextEntry={true}
              maxLength={30}
              onChangeText={(password) => setPassword(password)}
              onPressIn={() => setHasPassword(true)}
              value={password} />
            {!password && hasPassword ?
              <HelperText
                type="error"
                visible={!password && hasPassword}
                style={styles.validationError}>
                {t('validation.required', { 0: t('signup.user.password') })}
              </HelperText>
              : ''
            }
          </View>
        </View>
        <View style={styles.footer}>
          <Button
            mode="contained"
            style={styles.signupButton}
            onPress={onPressSignup}>
            {t('signup.button.signup')}
          </Button>
          <View style={styles.signin}>
            <Text variant='bodyMedium'>{t('signup.have.account')}</Text>
            <TouchableRipple onPress={onPressSignin}>
              <Text
                variant='bodyMedium'
                style={styles.signinText}>
                {t('signup.link.signin')}
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
    backgroundColor: '#fff',
  },
  body: {
    alignItems: 'center',
  },
  userInput: {
    width: "100%",
    alignItems: 'center',
    marginBottom: 30,
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
  footer: {
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 20,
  },
  signupButton: {
    width: "60%",
    backgroundColor: "#007bff",
  },
  signin: {
    flexDirection: 'row',
    marginTop: 40,
    marginBottom: 20,
  },
  signinText: {
    color: "#007bff",
    textDecorationLine: 'underline',
    marginLeft: 4
  },
});
