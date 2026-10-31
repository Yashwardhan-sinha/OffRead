package com.offread.app.data;

import android.content.Context;
import android.content.SharedPreferences;

/* JADX INFO: loaded from: classes.dex */
public class AppPreferences {
    public static final String APPEARANCE_DARK = "dark";
    public static final String APPEARANCE_LIGHT = "light";
    public static final String APPEARANCE_SYSTEM = "system";
    public static final String FONT_MONO = "mono";
    public static final String FONT_SANS = "sans";
    public static final String FONT_SERIF = "serif";
    public static final int FONT_SIZE_LARGE = 21;
    public static final int FONT_SIZE_MEDIUM = 18;
    public static final int FONT_SIZE_SMALL = 16;
    private static final String KEY_APPEARANCE = "app_appearance_mode";
    private static final String KEY_FIRST_RUN = "app_first_run";
    private static final String KEY_FONT_FAMILY = "reader_font_family";
    private static final String KEY_FONT_SIZE = "reader_font_size";
    private static final String KEY_FULLSCREEN = "reader_fullscreen";
    private static final String KEY_KEEP_SCREEN_ON = "reader_keep_screen_on";
    private static final String KEY_LINE_SPACING = "reader_line_spacing";
    private static final String KEY_READING_WIDTH = "reader_width";
    private static final String KEY_SPEED_WPM = "reader_speed_wpm";
    private static final String KEY_THEME = "reader_theme";
    private static final String KEY_TTS_LANGUAGE = "tts_language_tag";
    private static final String KEY_TTS_SPEED = "reader_tts_speed";
    private static final String PREF_NAME = "offread_preferences";
    public static final String THEME_AUTO = "auto";
    public static final String THEME_BLACK = "black";
    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_SEPIA = "sepia";
    public static final String TTS_LANGUAGE_AUTO = "";
    public static final String WIDTH_NORMAL = "normal";
    public static final String WIDTH_WIDE = "wide";
    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, 0);
    }

    public String getTheme() {
        return this.prefs.getString(KEY_THEME, THEME_AUTO);
    }

    public void setTheme(String str) {
        this.prefs.edit().putString(KEY_THEME, str).apply();
    }

    public String getFontSizePreset() {
        int fontSize = getFontSize();
        if (fontSize <= 17) {
            return "small";
        }
        if (fontSize >= 20) {
            return "large";
        }
        return "medium";
    }

    public void setFontSizePreset(String str) {
        if ("small".equals(str)) {
            setFontSize(16);
        } else if ("large".equals(str)) {
            setFontSize(21);
        } else {
            setFontSize(18);
        }
    }

    public String getFontFamily() {
        return this.prefs.getString(KEY_FONT_FAMILY, FONT_SERIF);
    }

    public void setFontFamily(String str) {
        this.prefs.edit().putString(KEY_FONT_FAMILY, str).apply();
    }

    public int getFontSize() {
        return this.prefs.getInt(KEY_FONT_SIZE, 18);
    }

    public void setFontSize(int i) {
        this.prefs.edit().putInt(KEY_FONT_SIZE, Math.max(12, Math.min(32, i))).apply();
    }

    public float getLineSpacing() {
        return this.prefs.getFloat(KEY_LINE_SPACING, 1.5f);
    }

    public void setLineSpacing(float f) {
        this.prefs.edit().putFloat(KEY_LINE_SPACING, f).apply();
    }

    public boolean isKeepScreenOn() {
        return this.prefs.getBoolean(KEY_KEEP_SCREEN_ON, true);
    }

    public void setKeepScreenOn(boolean z) {
        this.prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, z).apply();
    }

    public boolean isFullscreen() {
        return this.prefs.getBoolean(KEY_FULLSCREEN, false);
    }

    public void setFullscreen(boolean z) {
        this.prefs.edit().putBoolean(KEY_FULLSCREEN, z).apply();
    }

    public int getSpeedWpm() {
        return this.prefs.getInt(KEY_SPEED_WPM, 350);
    }

    public void setSpeedWpm(int i) {
        this.prefs.edit().putInt(KEY_SPEED_WPM, i).apply();
    }

    public float getTtsSpeed() {
        return this.prefs.getFloat(KEY_TTS_SPEED, 1.0f);
    }

    public void setTtsSpeed(float f) {
        this.prefs.edit().putFloat(KEY_TTS_SPEED, f).apply();
    }

    public String getAppearanceMode() {
        String string = this.prefs.getString(KEY_APPEARANCE, APPEARANCE_SYSTEM);
        return ("light".equals(string) || "dark".equals(string)) ? string : APPEARANCE_SYSTEM;
    }

    public void setAppearanceMode(String str) {
        this.prefs.edit().putString(KEY_APPEARANCE, str).apply();
    }

    public String getReadingWidth() {
        return WIDTH_WIDE.equals(this.prefs.getString(KEY_READING_WIDTH, WIDTH_NORMAL)) ? WIDTH_WIDE : WIDTH_NORMAL;
    }

    public void setReadingWidth(String str) {
        this.prefs.edit().putString(KEY_READING_WIDTH, str).apply();
    }

    public String getTtsLanguage() {
        return this.prefs.getString(KEY_TTS_LANGUAGE, TTS_LANGUAGE_AUTO);
    }

    public void setTtsLanguage(String str) {
        SharedPreferences.Editor editorEdit = this.prefs.edit();
        if (str == null) {
            str = TTS_LANGUAGE_AUTO;
        }
        editorEdit.putString(KEY_TTS_LANGUAGE, str).apply();
    }

    public boolean isFirstRun() {
        return this.prefs.getBoolean(KEY_FIRST_RUN, true);
    }

    public void setFirstRunCompleted() {
        this.prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();
    }
}
