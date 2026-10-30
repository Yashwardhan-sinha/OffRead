package com.offread.app.parser;

import com.offread.app.data.AppPreferences;

/* JADX INFO: loaded from: classes.dex */
public class ReadabilityResult {
    private final String author;
    private final String canonicalUrl;
    private final String content;
    private final String excerpt;
    private final String language;
    private final String leadImageUrl;
    private final String plainText;
    private final String publishedDate;
    private final int readingTimeMinutes;
    private final String siteName;
    private final String title;
    private final int wordCount;

    public ReadabilityResult(String str, String str2, String str3, String str4, String str5, int i, int i2) {
        this(str, str2, str3, str4, str5, i, i2, AppPreferences.TTS_LANGUAGE_AUTO, AppPreferences.TTS_LANGUAGE_AUTO, AppPreferences.TTS_LANGUAGE_AUTO, AppPreferences.TTS_LANGUAGE_AUTO, AppPreferences.TTS_LANGUAGE_AUTO);
    }

    public ReadabilityResult(String str, String str2, String str3, String str4, String str5, int i, int i2, String str6, String str7, String str8, String str9, String str10) {
        String strTrim;
        String strTrim2;
        String strTrim3;
        String strTrim4;
        String strTrim5;
        String strTrim6;
        String strTrim7;
        this.title = str != null ? str.trim() : "Untitled";
        String str11 = AppPreferences.TTS_LANGUAGE_AUTO;
        this.author = str2 != null ? str2.trim() : AppPreferences.TTS_LANGUAGE_AUTO;
        if (str3 == null) {
            strTrim = AppPreferences.TTS_LANGUAGE_AUTO;
        } else {
            strTrim = str3.trim();
        }
        this.content = strTrim;
        if (str4 == null) {
            strTrim2 = AppPreferences.TTS_LANGUAGE_AUTO;
        } else {
            strTrim2 = str4.trim();
        }
        this.plainText = strTrim2;
        if (str5 == null) {
            strTrim3 = AppPreferences.TTS_LANGUAGE_AUTO;
        } else {
            strTrim3 = str5.trim();
        }
        this.excerpt = strTrim3;
        this.wordCount = Math.max(0, i);
        this.readingTimeMinutes = Math.max(1, i2);
        if (str6 == null) {
            strTrim4 = AppPreferences.TTS_LANGUAGE_AUTO;
        } else {
            strTrim4 = str6.trim();
        }
        this.publishedDate = strTrim4;
        if (str7 == null) {
            strTrim5 = AppPreferences.TTS_LANGUAGE_AUTO;
        } else {
            strTrim5 = str7.trim();
        }
        this.leadImageUrl = strTrim5;
        if (str8 == null) {
            strTrim6 = AppPreferences.TTS_LANGUAGE_AUTO;
        } else {
            strTrim6 = str8.trim();
        }
        this.siteName = strTrim6;
        if (str9 == null) {
            strTrim7 = AppPreferences.TTS_LANGUAGE_AUTO;
        } else {
            strTrim7 = str9.trim();
        }
        this.canonicalUrl = strTrim7;
        this.language = str10 != null ? str10.trim() : str11;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getContent() {
        return this.content;
    }

    public String getPlainText() {
        return this.plainText;
    }

    public String getExcerpt() {
        return this.excerpt;
    }

    public int getWordCount() {
        return this.wordCount;
    }

    public int getReadingTimeMinutes() {
        return this.readingTimeMinutes;
    }

    public String getPublishedDate() {
        return this.publishedDate;
    }

    public String getLeadImageUrl() {
        return this.leadImageUrl;
    }

    public String getSiteName() {
        return this.siteName;
    }

    public String getCanonicalUrl() {
        return this.canonicalUrl;
    }

    public String getLanguage() {
        return this.language;
    }
}
