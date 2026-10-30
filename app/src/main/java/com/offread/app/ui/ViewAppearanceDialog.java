package com.offread.app.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import com.offread.app.util.ThemeManager;

/* JADX INFO: loaded from: classes.dex */
public class ViewAppearanceDialog {

    public interface AppearanceChangeListener {
        void onAppearanceChanged();
    }

    private interface ChipPredicate {
        boolean isActive(TextView textView);
    }

    public static void show(Activity activity, final AppPreferences appPreferences, final AppearanceChangeListener appearanceChangeListener) {
        final Dialog dialog = new Dialog(activity, ThemeManager.dialogTheme(activity));
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.dialog_appearance);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(-1, -2);
            window.setGravity(80);
            window.addFlags(2);
            window.setDimAmount(0.5f);
        }
        final TextView[] textViewArr = {(TextView) dialog.findViewById(R.id.btn_theme_auto), (TextView) dialog.findViewById(R.id.btn_theme_light), (TextView) dialog.findViewById(R.id.btn_theme_sepia), (TextView) dialog.findViewById(R.id.btn_theme_dark), (TextView) dialog.findViewById(R.id.btn_theme_black)};
        final TextView[] textViewArr2 = {(TextView) dialog.findViewById(R.id.btn_font_serif), (TextView) dialog.findViewById(R.id.btn_font_sans), (TextView) dialog.findViewById(R.id.btn_font_mono)};
        final TextView[] textViewArr3 = {(TextView) dialog.findViewById(R.id.btn_spacing_1), (TextView) dialog.findViewById(R.id.btn_spacing_2), (TextView) dialog.findViewById(R.id.btn_spacing_3)};
        final TextView textView = (TextView) dialog.findViewById(R.id.tv_font_size_val);
        Button button = (Button) dialog.findViewById(R.id.btn_font_size_decrease);
        Button button2 = (Button) dialog.findViewById(R.id.btn_font_size_increase);
        CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.cb_keep_screen_on);
        CheckBox checkBox2 = (CheckBox) dialog.findViewById(R.id.cb_fullscreen);
        textView.setText(appPreferences.getFontSize() + "sp");
        checkBox.setChecked(appPreferences.isKeepScreenOn());
        checkBox2.setChecked(appPreferences.isFullscreen());
        paintPaper(textViewArr, appPreferences.getTheme());
        paintFont(textViewArr2, appPreferences.getFontFamily());
        paintSpacing(textViewArr3, appPreferences.getLineSpacing());
        int i = 0;
        for (int i2 = 5; i < i2; i2 = 5) {
            final TextView textView2 = textViewArr[i];
            textView2.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ViewAppearanceDialog.1
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    ViewAppearanceDialog.lambda$show$0(textView2, appPreferences, textViewArr, appearanceChangeListener, view);
                }
            });
            i++;
        }
        for (int i3 = 0; i3 < 3; i3++) {
            final TextView textView3 = textViewArr2[i3];
            textView3.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ViewAppearanceDialog.2
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    ViewAppearanceDialog.lambda$show$1(textView3, appPreferences, textViewArr2, appearanceChangeListener, view);
                }
            });
        }
        for (int i4 = 0; i4 < 3; i4++) {
            final TextView textView4 = textViewArr3[i4];
            textView4.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ViewAppearanceDialog.3
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    ViewAppearanceDialog.lambda$show$2(textView4, appPreferences, textViewArr3, appearanceChangeListener, view);
                }
            });
        }
        button.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ViewAppearanceDialog.4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ViewAppearanceDialog.lambda$show$3(appPreferences, textView, appearanceChangeListener, view);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ViewAppearanceDialog.5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ViewAppearanceDialog.lambda$show$4(appPreferences, textView, appearanceChangeListener, view);
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.offread.app.ui.ViewAppearanceDialog.6
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                ViewAppearanceDialog.lambda$show$5(appPreferences, appearanceChangeListener, compoundButton, z);
            }
        });
        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.offread.app.ui.ViewAppearanceDialog.7
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                ViewAppearanceDialog.lambda$show$6(appPreferences, appearanceChangeListener, compoundButton, z);
            }
        });
        dialog.findViewById(R.id.btn_appearance_done).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.ViewAppearanceDialog.8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    static /* synthetic */ void lambda$show$0(TextView textView, AppPreferences appPreferences, TextView[] textViewArr, AppearanceChangeListener appearanceChangeListener, View view) {
        String strPaperKeyFor = paperKeyFor(textView);
        appPreferences.setTheme(strPaperKeyFor);
        paintPaper(textViewArr, strPaperKeyFor);
        if (appearanceChangeListener != null) {
            appearanceChangeListener.onAppearanceChanged();
        }
    }

    static /* synthetic */ void lambda$show$1(TextView textView, AppPreferences appPreferences, TextView[] textViewArr, AppearanceChangeListener appearanceChangeListener, View view) {
        String strFontKeyFor = fontKeyFor(textView);
        appPreferences.setFontFamily(strFontKeyFor);
        paintFont(textViewArr, strFontKeyFor);
        if (appearanceChangeListener != null) {
            appearanceChangeListener.onAppearanceChanged();
        }
    }

    static /* synthetic */ void lambda$show$2(TextView textView, AppPreferences appPreferences, TextView[] textViewArr, AppearanceChangeListener appearanceChangeListener, View view) {
        float fSpacingValue = spacingValue(textView);
        appPreferences.setLineSpacing(fSpacingValue);
        paintSpacing(textViewArr, fSpacingValue);
        if (appearanceChangeListener != null) {
            appearanceChangeListener.onAppearanceChanged();
        }
    }

    static /* synthetic */ void lambda$show$3(AppPreferences appPreferences, TextView textView, AppearanceChangeListener appearanceChangeListener, View view) {
        int fontSize = appPreferences.getFontSize() - 2;
        if (fontSize < 12) {
            return;
        }
        appPreferences.setFontSize(fontSize);
        textView.setText(fontSize + "sp");
        if (appearanceChangeListener != null) {
            appearanceChangeListener.onAppearanceChanged();
        }
    }

    static /* synthetic */ void lambda$show$4(AppPreferences appPreferences, TextView textView, AppearanceChangeListener appearanceChangeListener, View view) {
        int fontSize = appPreferences.getFontSize() + 2;
        if (fontSize > 32) {
            return;
        }
        appPreferences.setFontSize(fontSize);
        textView.setText(fontSize + "sp");
        if (appearanceChangeListener != null) {
            appearanceChangeListener.onAppearanceChanged();
        }
    }

    static /* synthetic */ void lambda$show$5(AppPreferences appPreferences, AppearanceChangeListener appearanceChangeListener, CompoundButton compoundButton, boolean z) {
        appPreferences.setKeepScreenOn(z);
        if (appearanceChangeListener != null) {
            appearanceChangeListener.onAppearanceChanged();
        }
    }

    static /* synthetic */ void lambda$show$6(AppPreferences appPreferences, AppearanceChangeListener appearanceChangeListener, CompoundButton compoundButton, boolean z) {
        appPreferences.setFullscreen(z);
        if (appearanceChangeListener != null) {
            appearanceChangeListener.onAppearanceChanged();
        }
    }

    private static String paperKeyFor(TextView textView) {
        int id = textView.getId();
        if (id == R.id.btn_theme_auto) {
            return AppPreferences.THEME_AUTO;
        }
        if (id == R.id.btn_theme_light) {
            return "light";
        }
        if (id == R.id.btn_theme_sepia) {
            return AppPreferences.THEME_SEPIA;
        }
        if (id == R.id.btn_theme_black) {
            return AppPreferences.THEME_BLACK;
        }
        return "dark";
    }

    private static String fontKeyFor(TextView textView) {
        int id = textView.getId();
        if (id == R.id.btn_font_sans) {
            return AppPreferences.FONT_SANS;
        }
        if (id == R.id.btn_font_mono) {
            return AppPreferences.FONT_MONO;
        }
        return AppPreferences.FONT_SERIF;
    }

    private static float spacingValue(TextView textView) {
        int id = textView.getId();
        if (id == R.id.btn_spacing_1) {
            return 1.2f;
        }
        return id == R.id.btn_spacing_3 ? 1.8f : 1.5f;
    }

    static /* synthetic */ boolean lambda$paintPaper$8(String str, TextView textView) {
        return paperKeyFor(textView).equals(str);
    }

    private static void paintPaper(TextView[] textViewArr, final String str) {
        paintChips(textViewArr, new ChipPredicate() { // from class: com.offread.app.ui.ViewAppearanceDialog.10
            @Override // com.offread.app.ui.ViewAppearanceDialog.ChipPredicate
            public final boolean isActive(TextView textView) {
                return ViewAppearanceDialog.lambda$paintPaper$8(str, textView);
            }
        });
    }

    static /* synthetic */ boolean lambda$paintFont$9(String str, TextView textView) {
        return fontKeyFor(textView).equals(str);
    }

    private static void paintFont(TextView[] textViewArr, final String str) {
        paintChips(textViewArr, new ChipPredicate() { // from class: com.offread.app.ui.ViewAppearanceDialog.0
            @Override // com.offread.app.ui.ViewAppearanceDialog.ChipPredicate
            public final boolean isActive(TextView textView) {
                return ViewAppearanceDialog.lambda$paintFont$9(str, textView);
            }
        });
    }

    static /* synthetic */ boolean lambda$paintSpacing$10(float f, TextView textView) {
        return Math.abs(spacingValue(textView) - f) < 0.05f;
    }

    private static void paintSpacing(TextView[] textViewArr, final float f) {
        paintChips(textViewArr, new ChipPredicate() { // from class: com.offread.app.ui.ViewAppearanceDialog.9
            @Override // com.offread.app.ui.ViewAppearanceDialog.ChipPredicate
            public final boolean isActive(TextView textView) {
                return ViewAppearanceDialog.lambda$paintSpacing$10(f, textView);
            }
        });
    }

    private static void paintChips(TextView[] textViewArr, ChipPredicate chipPredicate) {
        Context context = textViewArr[0].getContext();
        Activity activity = context instanceof Activity ? (Activity) context : null;
        int iColor = activity == null ? -16777216 : ThemeManager.color(activity, R.attr.colorTextPrimary);
        int iColor2 = activity != null ? ThemeManager.color(activity, R.attr.colorTextSecondary) : -16777216;
        for (TextView textView : textViewArr) {
            boolean zIsActive = chipPredicate.isActive(textView);
            textView.setBackgroundResource(zIsActive ? R.drawable.bg_chip_active : R.drawable.bg_chip);
            textView.setTextColor(zIsActive ? iColor : iColor2);
        }
    }
}
