package com.offread.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import com.offread.app.tts.SpeechLanguages;
import com.offread.app.util.ThemeManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public final class SpeechLanguageDialog {

    public interface OnPickedListener {
        void onPicked(String str, Locale locale);
    }

    private SpeechLanguageDialog() {
    }

    public static void show(final Activity activity, final AppPreferences appPreferences, final OnPickedListener onPickedListener) {
        SpeechLanguages.load(activity, new SpeechLanguages.Callback() { // from class: com.offread.app.ui.SpeechLanguageDialog.0
            @Override // com.offread.app.tts.SpeechLanguages.Callback
            public final void onResult(List list, boolean z) {
                SpeechLanguageDialog.lambda$show$0(activity, appPreferences, onPickedListener, list, z);
            }
        });
    }

    static /* synthetic */ void lambda$show$0(Activity activity, AppPreferences appPreferences, OnPickedListener onPickedListener, List list, boolean z) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        if (!z) {
            explainNoEngine(activity);
        } else {
            openList(activity, appPreferences, list, onPickedListener);
        }
    }

    private static void openList(Activity activity, final AppPreferences appPreferences, List<Locale> list, final OnPickedListener onPickedListener) {
        final ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(activity.getString(R.string.tts_language_auto));
        for (Locale locale : list) {
            arrayList.add(locale);
            arrayList2.add(SpeechLanguages.displayLabel(locale));
        }
        if (arrayList2.size() == 1) {
            new AlertDialog.Builder(activity, ThemeManager.alertTheme(activity)).setTitle(R.string.tts_language_dialog_title).setMessage(R.string.tts_no_voices).setPositiveButton(R.string.action_cancel, (DialogInterface.OnClickListener) null).show();
            return;
        }
        new AlertDialog.Builder(activity, ThemeManager.alertTheme(activity)).setTitle(R.string.tts_language_dialog_title).setSingleChoiceItems((String[]) arrayList2.toArray(new String[0]), checkedIndex(activity, appPreferences.getTtsLanguage(), arrayList), new DialogInterface.OnClickListener() { // from class: com.offread.app.ui.SpeechLanguageDialog.2
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                SpeechLanguageDialog.lambda$openList$1(arrayList, appPreferences, onPickedListener, dialogInterface, i);
            }
        }).setNegativeButton(R.string.action_cancel, (DialogInterface.OnClickListener) null).show();
    }

    static /* synthetic */ void lambda$openList$1(List list, AppPreferences appPreferences, OnPickedListener onPickedListener, DialogInterface dialogInterface, int i) {
        String strTagOf;
        if (i == 0) {
            strTagOf = AppPreferences.TTS_LANGUAGE_AUTO;
        } else {
            strTagOf = SpeechLanguages.tagOf((Locale) list.get(i - 1));
        }
        Locale locale = i == 0 ? Locale.getDefault() : (Locale) list.get(i - 1);
        appPreferences.setTtsLanguage(strTagOf);
        if (onPickedListener != null) {
            onPickedListener.onPicked(strTagOf, locale);
        }
        dialogInterface.dismiss();
    }

    private static int checkedIndex(Context context, String str, List<Locale> list) {
        if (str != null && !str.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                if (str.equals(SpeechLanguages.tagOf(list.get(i)))) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    private static void explainNoEngine(final Activity activity) {
        new AlertDialog.Builder(activity, ThemeManager.alertTheme(activity)).setTitle(R.string.pref_tts_language).setMessage(R.string.tts_none_installed).setPositiveButton(R.string.action_cancel, (DialogInterface.OnClickListener) null).setNeutralButton(R.string.action_install_voices, new DialogInterface.OnClickListener() { // from class: com.offread.app.ui.SpeechLanguageDialog.1
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                SpeechLanguageDialog.openEngineSettings(activity);
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void openEngineSettings(Activity activity) {
        if (tryLaunch(activity, new Intent("com.android.settings.TTS_SETTINGS"))) {
            return;
        }
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        if (tryLaunch(activity, intent) || tryLaunch(activity, new Intent("android.settings.SETTINGS"))) {
            return;
        }
        Toast.makeText(activity, R.string.tts_none_installed, 1).show();
    }

    private static boolean tryLaunch(Activity activity, Intent intent) {
        try {
            activity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException unused) {
            return false;
        }
    }
}
