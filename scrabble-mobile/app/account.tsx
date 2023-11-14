import * as ImagePicker from "expo-image-picker";
import { ReactElement, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { TouchableOpacity } from "react-native-gesture-handler";
import { ActivityIndicator, Avatar, Button, Chip, HelperText, Modal, PaperProvider, Portal, Text, TextInput } from 'react-native-paper';

import { Header } from '../components/layout/header';
import { Notification } from '../components/layout/notification';

import UserService from '../services/user.service';

import { User } from '../model/user';

interface Language {
  name: string,
  code: string,
};

const fakePassword = "0000000000";

export default function UserScreen() {

  const { t, i18n } = useTranslation();

  const [header, setHeader] = useState<ReactElement>();
  const [password, setPassword] = useState<string>();
  const [preferredLanguage, setPreferredLanguage] = useState<string>();
  const [profilePictureURI, setProfilePictureURI] = useState<string>();
  const [userLoaded, setUserLoaded] = useState<boolean>(false);
  const [showProfilePictureDialog, setShowProfilePictureDialog] = useState<boolean>(false);
  const [saveInProgress, setSaveInProgress] = useState<boolean>(false);

  const userRef = useRef<User>();
  const selectedProfilePictureRef = useRef<ImagePicker.ImagePickerAsset>();
  const uploadInProgressRef = useRef<boolean>(false);
  const notificationRef = useRef(null);

  const preferredLanguages: Language[] = [
    { name: t('language.en'), code: 'en', },
    { name: t('language.fr'), code: 'fr', },
    { name: t('language.de'), code: 'de', },
    { name: t('language.nl'), code: 'nl', },
    { name: t('language.tr'), code: 'tr', },
  ];

  useEffect(() => {
    if (userRef.current) {
      setUserLoaded(true);
      return;
    }

    async function loadUser(): Promise<void> {
      userRef.current = await UserService.get();
      userRef.current.password = fakePassword;
      setPassword(userRef.current.password);
      setPreferredLanguage(userRef.current.preferredLanguage);
      setUserLoaded(true);
      createHeader();
    }

    loadUser();

    return () => {
      setUserLoaded(false);
    };

  });

  function createHeader() {
    setHeader(
      <Header
        key={'header_' + userRef.current.preferredLanguage}
        title={t('user.account.title')}
        previousScreen='menu' />
    );
  }

  function isLanguageSelected(selectedPreferredLanguage: string): boolean {
    return preferredLanguage === selectedPreferredLanguage;
  }

  function onChangePassword(password: string): void {
    if (password.startsWith(fakePassword)) {
      password = password.replace(fakePassword, '');
    }
    setPassword(password);
  }

  async function onPressImageUpload(): Promise<void> {
    if (uploadInProgressRef.current) {
      notificationRef.current.warning(t('user.account.profile.picture.upload.in.progress'));
      return;
    }

    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();

    if (status !== "granted") {
      notificationRef.current.error(t('user.account.profile.picture.rejected'));
    } else {
      const imagePickerResult = await ImagePicker.launchImageLibraryAsync();
      if (!imagePickerResult.canceled) {
        selectedProfilePictureRef.current = imagePickerResult.assets[0];
        setProfilePictureURI(selectedProfilePictureRef.current.uri);
      }
    }
  }

  function onShowProfilePictureDialog(): void {
    setProfilePictureURI(process.env.EXPO_PUBLIC_PROFILE_PICTURE_URL + userRef.current.id + '?' + new Date().getTime());
    setShowProfilePictureDialog(true);
  }

  function onHideProfilePictureDialog(): void {
    if (uploadInProgressRef.current) {
      notificationRef.current.warning(t('user.account.profile.picture.upload.in.progress'));
      return;
    }

    selectedProfilePictureRef.current = null;
    setShowProfilePictureDialog(false);
  }

  function onPressUpdateProfilePicture(): void {
    if (uploadInProgressRef.current) {
      notificationRef.current.warning(t('user.account.profile.picture.upload.in.progress'));
      return;
    }

    uploadInProgressRef.current = true;
    setSaveInProgress(true);
    UserService.updateProfilePicture(selectedProfilePictureRef.current.uri).then(() => {
      notificationRef.current.success(t('user.account.profile.picture.updated'));
      uploadInProgressRef.current = false;

      // close the dialog
      onHideProfilePictureDialog();

      // update the header
      createHeader();
      setSaveInProgress(false);
    }).catch((error) => {
      uploadInProgressRef.current = false;
      notificationRef.current.error(error.toString());
      setSaveInProgress(false);
    });
  }

  function onPressUpdate(): void {
    if (!password) {
      return;
    }

    // do not update the password if it's not been changed
    const updatedPassword = (fakePassword === password ? null : password);

    UserService.update(userRef.current.username, userRef.current.email, updatedPassword, preferredLanguage).then(() => {
      // change the language if it's been updated
      if (preferredLanguage !== userRef.current.preferredLanguage) {
        i18n.changeLanguage(preferredLanguage);
      }

      // update the user reference
      userRef.current.password = updatedPassword;
      userRef.current.preferredLanguage = preferredLanguage;

      // show information
      notificationRef.current.success(t('user.account.message.updated'));

      // update the header
      createHeader();
    }).catch((error) => {
      notificationRef.current.error(error.toString());
    });
  }

  if (!userLoaded || !header) {
    return <ActivityIndicator animating={true} />;
  }

  return (
    <PaperProvider>
      <Portal>
        <Modal
          visible={showProfilePictureDialog}
          onDismiss={() => onHideProfilePictureDialog()}
          contentContainerStyle={styles.profilePictureDialog}>
          <Avatar.Image
            source={{ uri: profilePictureURI }}
            size={210} />
          {
            saveInProgress ?
              <ActivityIndicator
                animating={true}
                size='large'
                color="007bff"
                style={styles.saveIndicator} />
              :
              <View style={styles.profilePictureDialogButtons}>
                <Button
                  mode="contained"
                  style={styles.profilePictureDialogButton}
                  onPress={onPressImageUpload}>
                  {t('user.account.button.select')}
                </Button>
                {
                  selectedProfilePictureRef.current ?
                    <Button
                      mode="contained"
                      style={styles.profilePictureDialogButton}
                      onPress={onPressUpdateProfilePicture}>
                      {t('user.account.button.upload')}
                    </Button>
                    : ''
                }
              </View>
          }

        </Modal>
      </Portal>
      {header}
      <View style={styles.container}>
        <View style={styles.body}>
          <View style={styles.userInput}>
            <View style={styles.label}>
              <Avatar.Icon
                size={24}
                icon="badge-account-outline"
                style={styles.labelIcon} />
              <Text
                variant="bodyLarge"
                style={styles.labelText}>
                {t('user.account.profile.picture')}
              </Text>
            </View>
            <TouchableOpacity onPress={() => onShowProfilePictureDialog()}>
              <Avatar.Image
                source={{ uri: process.env.EXPO_PUBLIC_PROFILE_PICTURE_URL + userRef.current.id + '?' + new Date().getTime() }}
                size={72} />
            </TouchableOpacity>
          </View>
          <View style={styles.userInput}>
            <View style={styles.label}>
              <Avatar.Icon
                size={24}
                icon="account-outline"
                style={styles.labelIcon} />
              <Text
                variant="bodyLarge"
                style={styles.labelText}>
                {t('user.account.user.username')}
              </Text>
            </View>
            <TextInput
              style={styles.inputText}
              outlineStyle={styles.inputTextOutline}
              cursorColor='#007bff'
              mode='outlined'
              maxLength={30}
              disabled={true}
              value={userRef.current.username} />
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
                {t('user.account.user.email')}
              </Text>
            </View>
            <TextInput
              style={styles.inputText}
              outlineStyle={styles.inputTextOutline}
              cursorColor='#007bff'
              mode='outlined'
              maxLength={30}
              disabled={true}
              value={userRef.current.email} />
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
                {t('user.account.user.password')}
              </Text>
            </View>
            <TextInput
              style={styles.inputText}
              outlineStyle={styles.inputTextOutline}
              cursorColor='#007bff'
              placeholder={t('user.account.user.password.placeholder')}
              mode='outlined'
              secureTextEntry={true}
              maxLength={30}
              onChangeText={(password) => onChangePassword(password)}
              value={password} />
            {!password ?
              <HelperText
                type="error"
                visible={!password}
                style={styles.validationError}>
                {t('validation.required', { 0: t('user.account.user.password') })}
              </HelperText>
              : ''
            }
          </View>
          <View style={styles.userInput}>
            <View style={styles.label}>
              <Avatar.Icon
                size={24}
                icon="flag-outline"
                style={styles.labelIcon} />
              <Text
                variant="bodyLarge"
                style={styles.labelText}>
                {t('user.account.user.language')}
              </Text>
            </View>
            <View style={styles.chipView}>
              {
                preferredLanguages.map((language: Language) => (
                  <Chip
                    key={'language_' + language.code}
                    style={styles.chip}
                    textStyle={styles.chipText}
                    selectedColor='#007bff'
                    onPress={() => setPreferredLanguage(language.code)}
                    selected={isLanguageSelected(language.code)}>
                    {language.name}
                  </Chip>
                ))
              }
            </View>
          </View>
        </View>
        <View style={styles.footer}>
          <Button
            mode="contained"
            style={styles.updateButton}
            onPress={onPressUpdate}>
            {t('user.account.button.update')}
          </Button>
        </View>
      </View>
      <Notification notificationRef={notificationRef} />
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
    marginTop: 30,
  },
  label: {
    flexDirection: 'row',
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
  footer: {
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 40,
  },
  updateButton: {
    width: "60%",
    backgroundColor: "#007bff",
  },
  profilePictureDialog: {
    backgroundColor: '#d8d8d8',
    borderColor: '#d8d8d8',
    borderWidth: 1,
    borderRadius: 7,
    justifyContent: 'center',
    alignItems: 'center',
    paddingTop: 20,
    paddingBottom: 20,
    margin: 40,
  },
  profilePictureDialogButtons: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  profilePictureDialogButton: {
    width: "100%",
    backgroundColor: "#007bff",
    marginTop: 30,
  },
  saveIndicator: {
    marginTop: 30,
  },
});
