package com.offread.app.util;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;
import android.view.Window;
import com.offread.app.R;
import com.offread.app.data.AppPreferences;

/* JADX INFO: loaded from: classes.dex */
public class UIHelper {

    public static class ReaderThemeColors {
        public final int backgroundColor;
        public final boolean isLight;
        public final int secondaryTextColor;
        public final int surfaceColor;
        public final int textColor;

        public ReaderThemeColors(int i, int i2, int i3, int i4, boolean z) {
            this.backgroundColor = i;
            this.textColor = i2;
            this.secondaryTextColor = i3;
            this.surfaceColor = i4;
            this.isLight = z;
        }
    }

    public static ReaderThemeColors getThemeColors(String str, Activity activity) {
        if (activity != null && (str == null || AppPreferences.THEME_AUTO.equals(str))) {
            return new ReaderThemeColors(ThemeManager.color(activity, R.attr.colorBackground), ThemeManager.color(activity, R.attr.colorTextPrimary), ThemeManager.color(activity, R.attr.colorTextSecondary), ThemeManager.color(activity, R.attr.colorSurface), !(ThemeManager.nightMode(activity) == 1));
        }
        return getThemeColors(str);
    }

    public static ReaderThemeColors getThemeColors(String str) {
        if (AppPreferences.THEME_SEPIA.equals(str)) {
            return new ReaderThemeColors(-725800, -12372958, -8754603, -1515575, true);
        }
        if ("light".equals(str)) {
            return new ReaderThemeColors(-328967, -14935785, -8883860, -1053466, true);
        }
        if (AppPreferences.THEME_BLACK.equals(str)) {
            return new ReaderThemeColors(-16777216, -1710619, -9211021, -15592942, false);
        }
        return new ReaderThemeColors(-15788246, -1906448, -7035976, -14800581, false);
    }

    public static Typeface getTypeface(String str) {
        if (AppPreferences.FONT_SANS.equals(str)) {
            return Typeface.SANS_SERIF;
        }
        if (AppPreferences.FONT_MONO.equals(str)) {
            return Typeface.MONOSPACE;
        }
        return Typeface.SERIF;
    }

    public static void applySystemBars(Activity activity, int i, int i2, boolean z) {
        int i3;
        if (activity == null) {
            return;
        }
        Window window = activity.getWindow();
        window.addFlags(Integer.MIN_VALUE);
        window.setStatusBarColor(i);
        window.setNavigationBarColor(i2);
        View decorView = window.getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        if (z) {
            i3 = systemUiVisibility | 8192;
            if (Build.VERSION.SDK_INT >= 26) {
                i3 = systemUiVisibility | 8208;
            }
        } else {
            i3 = systemUiVisibility & (-8193);
            if (Build.VERSION.SDK_INT >= 26) {
                i3 = systemUiVisibility & (-8209);
            }
        }
        decorView.setSystemUiVisibility(i3);
    }
}
