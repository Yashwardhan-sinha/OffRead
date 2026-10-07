package com.offread.app.data;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private static final String PREF_NAME = "offread_preferences";

    public static final String THEME_DARK = "dark";
    public static final String THEME_SEPIA = "sepia";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_BLACK = "black";

    public static final String FONT_SERIF = "serif";
    public static final String FONT_SANS = "sans";
    public static final String FONT_MONO = "mono";

    private static final String KEY_THEME = "reader_theme";
    private static final String KEY_FONT_FAMILY = "reader_font_family";
    private static final String KEY_FONT_SIZE = "reader_font_size";
    private static final String KEY_LINE_SPACING = "reader_line_spacing";
    private static final String KEY_KEEP_SCREEN_ON = "reader_keep_screen_on";
    private static final String KEY_FULLSCREEN = "reader_fullscreen";
    private static final String KEY_SPEED_WPM = "reader_speed_wpm";
    private static final String KEY_TTS_SPEED = "reader_tts_speed";
    private static final String KEY_FIRST_RUN = "app_first_run";

    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getTheme() {
        return prefs.getString(KEY_THEME, THEME_DARK);
    }

    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    public String getFontFamily() {
        return prefs.getString(KEY_FONT_FAMILY, FONT_SERIF);
    }

    public void setFontFamily(String font) {
        prefs.edit().putString(KEY_FONT_FAMILY, font).apply();
    }

    public int getFontSize() {
        return prefs.getInt(KEY_FONT_SIZE, 18);
    }

    public void setFontSize(int sizeSp) {
        int bounded = Math.max(12, Math.min(32, sizeSp));
        prefs.edit().putInt(KEY_FONT_SIZE, bounded).apply();
    }

    public float getLineSpacing() {
        return prefs.getFloat(KEY_LINE_SPACING, 1.5f);
    }

    public void setLineSpacing(float spacing) {
        prefs.edit().putFloat(KEY_LINE_SPACING, spacing).apply();
    }

    public boolean isKeepScreenOn() {
        return prefs.getBoolean(KEY_KEEP_SCREEN_ON, true);
    }

    public void setKeepScreenOn(boolean keepOn) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, keepOn).apply();
    }

    public boolean isFullscreen() {
        return prefs.getBoolean(KEY_FULLSCREEN, false);
    }

    public void setFullscreen(boolean fullscreen) {
        prefs.edit().putBoolean(KEY_FULLSCREEN, fullscreen).apply();
    }

    public int getSpeedWpm() {
        return prefs.getInt(KEY_SPEED_WPM, 350);
    }

    public void setSpeedWpm(int wpm) {
        prefs.edit().putInt(KEY_SPEED_WPM, wpm).apply();
    }

    public float getTtsSpeed() {
        return prefs.getFloat(KEY_TTS_SPEED, 1.0f);
    }

    public void setTtsSpeed(float speed) {
        prefs.edit().putFloat(KEY_TTS_SPEED, speed).apply();
    }

    public boolean isFirstRun() {
        return prefs.getBoolean(KEY_FIRST_RUN, true);
    }

    public void setFirstRunCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();
    }
}
