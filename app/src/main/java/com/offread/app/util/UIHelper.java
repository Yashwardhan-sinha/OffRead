package com.offread.app.util;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.offread.app.data.AppPreferences;

public class UIHelper {

    public static class ReaderThemeColors {
        public final int backgroundColor;
        public final int textColor;
        public final int secondaryTextColor;
        public final int surfaceColor;
        public final boolean isLight;

        public ReaderThemeColors(int bg, int text, int secondary, int surface, boolean light) {
            this.backgroundColor = bg;
            this.textColor = text;
            this.secondaryTextColor = secondary;
            this.surfaceColor = surface;
            this.isLight = light;
        }
    }

    public static ReaderThemeColors getThemeColors(String themeKey) {
        if (AppPreferences.THEME_SEPIA.equals(themeKey)) {
            return new ReaderThemeColors(
                    0xFFF4ECD8,
                    0xFF433422,
                    0xFF7A6A55,
                    0xFFE8DFC9,
                    true
            );
        } else if (AppPreferences.THEME_LIGHT.equals(themeKey)) {
            return new ReaderThemeColors(
                    0xFFFAFAF9,
                    0xFF1C1917,
                    0xFF78716C,
                    0xFFEFECE6,
                    true
            );
        } else if (AppPreferences.THEME_BLACK.equals(themeKey)) {
            return new ReaderThemeColors(
                    0xFF000000,
                    0xFFE5E5E5,
                    0xFF737373,
                    0xFF121212,
                    false
            );
        } else {
            // Default: Dark slate
            return new ReaderThemeColors(
                    0xFF0F172A,
                    0xFFE2E8F0,
                    0xFF94A3B8,
                    0xFF1E293B,
                    false
            );
        }
    }

    public static Typeface getTypeface(String fontKey) {
        if (AppPreferences.FONT_SANS.equals(fontKey)) {
            return Typeface.SANS_SERIF;
        } else if (AppPreferences.FONT_MONO.equals(fontKey)) {
            return Typeface.MONOSPACE;
        } else {
            return Typeface.SERIF;
        }
    }

    public static void applySystemBars(Activity activity, int color, boolean isLight) {
        if (activity == null) return;
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
        window.setNavigationBarColor(color);

        View decor = window.getDecorView();
        int flags = decor.getSystemUiVisibility();
        if (isLight) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        } else {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        }
        decor.setSystemUiVisibility(flags);
    }
}
