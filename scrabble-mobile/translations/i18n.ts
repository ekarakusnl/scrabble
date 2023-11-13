import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import en from './en.json';
import fr from './fr.json';
import de from './de.json';
import nl from './nl.json';
import tr from './tr.json';

const resources = {
  en: { translation: en },
  fr: { translation: fr },
  de: { translation: de },
  nl: { translation: nl },
  tr: { translation: tr }
};

i18n
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false,  // react does not need this
    },
    compatibilityJSON: 'v3',
  });

export default i18n;
