package com.offread.app.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import com.offread.app.data.AppPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public final class SpeechLanguages {

    public interface Callback {
        void onResult(List<Locale> list, boolean z);
    }

    private SpeechLanguages() {
    }

    public static void load(Context context, final Callback callback) {
        if (context == null || callback == null) {
            return;
        }
        final TextToSpeech[] textToSpeechArr = new TextToSpeech[1];
        try {
            textToSpeechArr[0] = new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() { // from class: com.offread.app.tts.SpeechLanguages.1
                @Override // android.speech.tts.TextToSpeech.OnInitListener
                public void onInit(int i) {
                    int iIsLanguageAvailable;
                    ArrayList arrayList = new ArrayList();
                    boolean z = false;
                    TextToSpeech textToSpeech = textToSpeechArr[0];
                    if (i == 0 && textToSpeech != null) {
                        z = true;
                    }
                    if (z) {
                        try {
                            Set<Locale> availableLanguages = textToSpeech.getAvailableLanguages();
                            if (availableLanguages != null) {
                                for (Locale locale : availableLanguages) {
                                    if (locale != null && !locale.getLanguage().isEmpty() && (iIsLanguageAvailable = textToSpeech.isLanguageAvailable(locale)) != -2 && iIsLanguageAvailable != -1) {
                                        arrayList.add(locale);
                                    }
                                }
                            }
                        } catch (Throwable unused) {
                        }
                    }
                    if (textToSpeech != null) {
                        try {
                            textToSpeech.shutdown();
                        } catch (Throwable unused2) {
                        }
                    }
                    SpeechLanguages.sortDeterministically(arrayList);
                    callback.onResult(arrayList, z);
                }
            });
        } catch (Throwable unused) {
            callback.onResult(new ArrayList(), false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void sortDeterministically(List<Locale> list) {
        Collections.sort(list, new Comparator<Locale>() { // from class: com.offread.app.tts.SpeechLanguages.2
            @Override // java.util.Comparator
            public int compare(Locale locale, Locale locale2) {
                return SpeechLanguages.displayLabel(locale).compareToIgnoreCase(SpeechLanguages.displayLabel(locale2));
            }
        });
    }

    public static String displayLabel(Locale locale) {
        if (locale == null) {
            return AppPreferences.TTS_LANGUAGE_AUTO;
        }
        Locale locale2 = Locale.getDefault();
        String displayLanguage = locale.getDisplayLanguage(locale2);
        if (displayLanguage == null || displayLanguage.isEmpty()) {
            displayLanguage = locale.getLanguage();
        }
        String country = locale.getCountry();
        if (country == null || country.isEmpty()) {
            String variant = locale.getVariant();
            if (variant == null || variant.isEmpty()) {
                return displayLanguage;
            }
            return displayLanguage + " (" + variant + ")";
        }
        return displayLanguage + " (" + new Locale(AppPreferences.TTS_LANGUAGE_AUTO, country).getDisplayCountry(locale2) + ")";
    }

    public static String tagOf(Locale locale) {
        String language;
        if (locale == null || (language = locale.getLanguage()) == null || language.isEmpty()) {
            return AppPreferences.TTS_LANGUAGE_AUTO;
        }
        String country = locale.getCountry();
        if (country == null || country.isEmpty()) {
            return language;
        }
        return language + "-" + country;
    }

    public static Locale localeForTag(String str) {
        if (str != null && !str.trim().isEmpty()) {
            try {
                Locale localeForLanguageTag = Locale.forLanguageTag(str.trim());
                if (localeForLanguageTag.getLanguage().isEmpty()) {
                    return null;
                }
                return localeForLanguageTag;
            } catch (Throwable unused) {
            }
        }
        return null;
    }

    public static Locale fromHtmlLang(String str, List<Locale> list) {
        Locale localeLocaleForTag = localeForTag(str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str.replace('_', '-'));
        if (localeLocaleForTag == null) {
            return null;
        }
        if (list == null || list.isEmpty()) {
            return localeLocaleForTag;
        }
        for (Locale locale : list) {
            if (tagOf(locale).equalsIgnoreCase(tagOf(localeLocaleForTag))) {
                return locale;
            }
        }
        for (Locale locale2 : list) {
            if (locale2.getLanguage().equals(localeLocaleForTag.getLanguage())) {
                return locale2;
            }
        }
        return null;
    }
}
