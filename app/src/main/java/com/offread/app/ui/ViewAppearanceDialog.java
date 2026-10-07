package com.offread.app.ui;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.offread.app.R;
import com.offread.app.data.AppPreferences;

public class ViewAppearanceDialog {

    public interface AppearanceChangeListener {
        void onAppearanceChanged();
    }

    public static void show(Activity activity, AppPreferences prefs, AppearanceChangeListener listener) {
        Dialog dialog = new Dialog(activity, R.style.Theme_OffRead_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_appearance);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }

        // Theme buttons
        TextView btnDark = dialog.findViewById(R.id.btn_theme_dark);
        TextView btnSepia = dialog.findViewById(R.id.btn_theme_sepia);
        TextView btnLight = dialog.findViewById(R.id.btn_theme_light);
        TextView btnBlack = dialog.findViewById(R.id.btn_theme_black);

        // Font buttons
        TextView btnSerif = dialog.findViewById(R.id.btn_font_serif);
        TextView btnSans = dialog.findViewById(R.id.btn_font_sans);
        TextView btnMono = dialog.findViewById(R.id.btn_font_mono);

        // Font size
        Button btnSizeDec = dialog.findViewById(R.id.btn_font_size_decrease);
        Button btnSizeInc = dialog.findViewById(R.id.btn_font_size_increase);
        TextView tvSizeVal = dialog.findViewById(R.id.tv_font_size_val);

        // Line spacing
        TextView btnSpacing1 = dialog.findViewById(R.id.btn_spacing_1);
        TextView btnSpacing2 = dialog.findViewById(R.id.btn_spacing_2);
        TextView btnSpacing3 = dialog.findViewById(R.id.btn_spacing_3);

        // Toggles
        CheckBox cbKeepOn = dialog.findViewById(R.id.cb_keep_screen_on);
        CheckBox cbFullscreen = dialog.findViewById(R.id.cb_fullscreen);

        // Initialize state
        tvSizeVal.setText(prefs.getFontSize() + "sp");
        cbKeepOn.setChecked(prefs.isKeepScreenOn());
        cbFullscreen.setChecked(prefs.isFullscreen());

        updateThemeChips(dialog, prefs.getTheme(), btnDark, btnSepia, btnLight, btnBlack);
        updateFontChips(dialog, prefs.getFontFamily(), btnSerif, btnSans, btnMono);
        updateSpacingChips(dialog, prefs.getLineSpacing(), btnSpacing1, btnSpacing2, btnSpacing3);

        // Click handlers
        btnDark.setOnClickListener(v -> {
            prefs.setTheme(AppPreferences.THEME_DARK);
            updateThemeChips(dialog, AppPreferences.THEME_DARK, btnDark, btnSepia, btnLight, btnBlack);
            if (listener != null) listener.onAppearanceChanged();
        });
        btnSepia.setOnClickListener(v -> {
            prefs.setTheme(AppPreferences.THEME_SEPIA);
            updateThemeChips(dialog, AppPreferences.THEME_SEPIA, btnDark, btnSepia, btnLight, btnBlack);
            if (listener != null) listener.onAppearanceChanged();
        });
        btnLight.setOnClickListener(v -> {
            prefs.setTheme(AppPreferences.THEME_LIGHT);
            updateThemeChips(dialog, AppPreferences.THEME_LIGHT, btnDark, btnSepia, btnLight, btnBlack);
            if (listener != null) listener.onAppearanceChanged();
        });
        btnBlack.setOnClickListener(v -> {
            prefs.setTheme(AppPreferences.THEME_BLACK);
            updateThemeChips(dialog, AppPreferences.THEME_BLACK, btnDark, btnSepia, btnLight, btnBlack);
            if (listener != null) listener.onAppearanceChanged();
        });

        btnSerif.setOnClickListener(v -> {
            prefs.setFontFamily(AppPreferences.FONT_SERIF);
            updateFontChips(dialog, AppPreferences.FONT_SERIF, btnSerif, btnSans, btnMono);
            if (listener != null) listener.onAppearanceChanged();
        });
        btnSans.setOnClickListener(v -> {
            prefs.setFontFamily(AppPreferences.FONT_SANS);
            updateFontChips(dialog, AppPreferences.FONT_SANS, btnSerif, btnSans, btnMono);
            if (listener != null) listener.onAppearanceChanged();
        });
        btnMono.setOnClickListener(v -> {
            prefs.setFontFamily(AppPreferences.FONT_MONO);
            updateFontChips(dialog, AppPreferences.FONT_MONO, btnSerif, btnSans, btnMono);
            if (listener != null) listener.onAppearanceChanged();
        });

        btnSizeDec.setOnClickListener(v -> {
            int current = prefs.getFontSize();
            if (current > 12) {
                int next = current - 2;
                prefs.setFontSize(next);
                tvSizeVal.setText(next + "sp");
                if (listener != null) listener.onAppearanceChanged();
            }
        });
        btnSizeInc.setOnClickListener(v -> {
            int current = prefs.getFontSize();
            if (current < 32) {
                int next = current + 2;
                prefs.setFontSize(next);
                tvSizeVal.setText(next + "sp");
                if (listener != null) listener.onAppearanceChanged();
            }
        });

        btnSpacing1.setOnClickListener(v -> {
            prefs.setLineSpacing(1.2f);
            updateSpacingChips(dialog, 1.2f, btnSpacing1, btnSpacing2, btnSpacing3);
            if (listener != null) listener.onAppearanceChanged();
        });
        btnSpacing2.setOnClickListener(v -> {
            prefs.setLineSpacing(1.5f);
            updateSpacingChips(dialog, 1.5f, btnSpacing1, btnSpacing2, btnSpacing3);
            if (listener != null) listener.onAppearanceChanged();
        });
        btnSpacing3.setOnClickListener(v -> {
            prefs.setLineSpacing(1.8f);
            updateSpacingChips(dialog, 1.8f, btnSpacing1, btnSpacing2, btnSpacing3);
            if (listener != null) listener.onAppearanceChanged();
        });

        cbKeepOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setKeepScreenOn(isChecked);
            if (listener != null) listener.onAppearanceChanged();
        });
        cbFullscreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setFullscreen(isChecked);
            if (listener != null) listener.onAppearanceChanged();
        });

        dialog.show();
    }

    private static void updateThemeChips(Dialog d, String active, TextView dark, TextView sepia, TextView light, TextView black) {
        dark.setBackgroundResource(AppPreferences.THEME_DARK.equals(active) ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        sepia.setBackgroundResource(AppPreferences.THEME_SEPIA.equals(active) ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        light.setBackgroundResource(AppPreferences.THEME_LIGHT.equals(active) ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        black.setBackgroundResource(AppPreferences.THEME_BLACK.equals(active) ? R.drawable.bg_chip_active : R.drawable.bg_chip);
    }

    private static void updateFontChips(Dialog d, String active, TextView serif, TextView sans, TextView mono) {
        serif.setBackgroundResource(AppPreferences.FONT_SERIF.equals(active) ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        sans.setBackgroundResource(AppPreferences.FONT_SANS.equals(active) ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        mono.setBackgroundResource(AppPreferences.FONT_MONO.equals(active) ? R.drawable.bg_chip_active : R.drawable.bg_chip);
    }

    private static void updateSpacingChips(Dialog d, float active, TextView s1, TextView s2, TextView s3) {
        s1.setBackgroundResource(Math.abs(active - 1.2f) < 0.05f ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        s2.setBackgroundResource(Math.abs(active - 1.5f) < 0.05f ? R.drawable.bg_chip_active : R.drawable.bg_chip);
        s3.setBackgroundResource(Math.abs(active - 1.8f) < 0.05f ? R.drawable.bg_chip_active : R.drawable.bg_chip);
    }
}
