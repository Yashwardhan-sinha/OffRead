package com.offread.app.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import com.offread.app.data.Article;
import com.offread.app.data.ArticleRepository;
import com.offread.app.data.BackupFileProvider;
import com.offread.app.data.OffReadDbHelper;
import com.offread.app.tts.SpeechLanguages;
import com.offread.app.util.BackupHelper;
import com.offread.app.util.SampleContentHelper;
import com.offread.app.util.ThemeManager;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: loaded from: classes.dex */
public class SettingsActivity extends ThemedActivity {
    private TextView engineState;
    private TextView[] fontChips;
    private TextView languageHint;
    private TextView languageValue;
    private AppPreferences preferences;
    private ArticleRepository repository;
    private TextView[] speedChips;
    private TextView[] themeChips;
    private TextView[] widthChips;
    private final ExecutorService backupExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override // com.offread.app.ui.ThemedActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_settings);
        applySystemBars(themeColor(R.attr.colorSurface));
        this.preferences = new AppPreferences(this);
        this.repository = ArticleRepository.getInstance(this);
        this.themeChips = new TextView[]{(TextView) findViewById(R.id.chip_theme_system), (TextView) findViewById(R.id.chip_theme_light), (TextView) findViewById(R.id.chip_theme_dark)};
        this.fontChips = new TextView[]{(TextView) findViewById(R.id.chip_font_small), (TextView) findViewById(R.id.chip_font_medium), (TextView) findViewById(R.id.chip_font_large)};
        this.widthChips = new TextView[]{(TextView) findViewById(R.id.chip_width_normal), (TextView) findViewById(R.id.chip_width_wide)};
        this.speedChips = new TextView[]{(TextView) findViewById(R.id.chip_speed_75), (TextView) findViewById(R.id.chip_speed_100), (TextView) findViewById(R.id.chip_speed_125), (TextView) findViewById(R.id.chip_speed_150)};
        this.languageValue = (TextView) findViewById(R.id.tv_tts_language_value);
        this.languageHint = (TextView) findViewById(R.id.tv_tts_language_hint);
        this.engineState = (TextView) findViewById(R.id.tv_tts_engine_state);
        TextView[][] textViewArr = {this.themeChips, this.fontChips, this.widthChips, this.speedChips};
        for (int i = 0; i < 4; i++) {
            for (final TextView textView : textViewArr[i]) {
                textView.setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SettingsActivity.4
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        SettingsActivity.this.lambda$onCreate$0(textView, view);
                    }
                });
            }
        }
        findViewById(R.id.row_tts_language).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SettingsActivity.5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SettingsActivity.this.lambda$onCreate$2(view);
            }
        });
        findViewById(R.id.row_export).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SettingsActivity.6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SettingsActivity.this.lambda$onCreate$3(view);
            }
        });
        findViewById(R.id.row_import).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SettingsActivity.7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SettingsActivity.this.lambda$onCreate$4(view);
            }
        });
        findViewById(R.id.row_samples).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SettingsActivity.8
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SettingsActivity.this.lambda$onCreate$5(view);
            }
        });
        findViewById(R.id.row_clear).setOnClickListener(new View.OnClickListener() { // from class: com.offread.app.ui.SettingsActivity.9
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SettingsActivity.this.lambda$onCreate$6(view);
            }
        });
        BottomNavBar bottomNavBar = (BottomNavBar) findViewById(R.id.nav_bar);
        bottomNavBar.setSelected(2);
        bottomNavBar.setOnTabSelectedListener(new BottomNavBar.OnTabSelectedListener() { // from class: com.offread.app.ui.SettingsActivity.10
            @Override // com.offread.app.ui.BottomNavBar.OnTabSelectedListener
            public final void onTabSelected(int i2) {
                SettingsActivity.this.lambda$onCreate$7(i2);
            }
        });
        this.languageValue.setText(labelForSavedLanguage(this.preferences.getTtsLanguage()));
        probeEngine();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0(TextView textView, View view) {
        onChipTapped(textView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$2(View view) {
        SpeechLanguageDialog.show(this, this.preferences, new SpeechLanguageDialog.OnPickedListener() { // from class: com.offread.app.ui.SettingsActivity.0
            @Override // com.offread.app.ui.SpeechLanguageDialog.OnPickedListener
            public final void onPicked(String str, Locale locale) {
                SettingsActivity.this.lambda$onCreate$1(str, locale);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$1(String str, Locale locale) {
        this.languageValue.setText(labelForSavedLanguage(str));
        Toast.makeText(this, getString(R.string.pref_tts_language) + ": " + labelForSavedLanguage(str), 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$3(View view) {
        handleExport();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$4(View view) {
        handleImport();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$5(View view) {
        handleRestoreSamples();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$6(View view) {
        handleClearAll();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$7(int i) {
        if (i != 2) {
            goToSection(i);
        }
    }

    @Override // com.offread.app.ui.ThemedActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        paintAll();
        bindAbout();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.backupExecutor.shutdown();
    }

    private void paintAll() {
        paintGroup(this.themeChips, keyForActiveTheme());
        paintGroup(this.fontChips, this.preferences.getFontSizePreset());
        paintGroup(this.widthChips, this.preferences.getReadingWidth());
        paintGroup(this.speedChips, speedKey(this.preferences.getTtsSpeed()));
        this.languageValue.setText(labelForSavedLanguage(this.preferences.getTtsLanguage()));
    }

    private String keyForActiveTheme() {
        return this.preferences.getAppearanceMode();
    }

    private void paintGroup(TextView[] textViewArr, String str) {
        int iColor = ThemeManager.color(this, R.attr.colorTextPrimary);
        int iColor2 = ThemeManager.color(this, R.attr.colorTextSecondary);
        for (TextView textView : textViewArr) {
            boolean zEquals = str.equals(keyFor(textView));
            textView.setBackgroundResource(zEquals ? R.drawable.bg_chip_active : R.drawable.bg_chip);
            textView.setTextColor(zEquals ? iColor : iColor2);
        }
    }

    private String speedKey(float f) {
        if (Math.abs(f - 0.75f) < 0.06f) {
            return "0.75";
        }
        if (Math.abs(f - 1.25f) < 0.06f) {
            return "1.25";
        }
        return Math.abs(f - 1.5f) < 0.06f ? "1.5" : "1.0";
    }

    private void bindAbout() {
        ((TextView) findViewById(R.id.tv_about_version)).setText(getString(R.string.about_version, new Object[]{versionName()}));
        ((TextView) findViewById(R.id.tv_about_library)).setText(getString(R.string.library_count_value, new Object[]{Integer.valueOf(this.repository.getArticles(ArticleRepository.FilterType.ALL, null, null, ArticleRepository.SortOrder.NEWEST).size())}));
    }

    private String versionName() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName == null ? AppPreferences.TTS_LANGUAGE_AUTO : packageInfo.versionName;
        } catch (Exception unused) {
            return AppPreferences.TTS_LANGUAGE_AUTO;
        }
    }

    private void probeEngine() {
        SpeechLanguages.load(this, new SpeechLanguages.Callback() { // from class: com.offread.app.ui.SettingsActivity.3
            @Override // com.offread.app.tts.SpeechLanguages.Callback
            public final void onResult(List list, boolean z) {
                SettingsActivity.this.lambda$probeEngine$8(list, z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$probeEngine$8(List list, boolean z) {
        if (isFinishing()) {
            return;
        }
        TextView textView = this.engineState;
        if (!z) {
            textView.setText(R.string.tts_none_installed);
            this.languageHint.setText(R.string.tts_language_unavailable);
        } else {
            textView.setText(getString(R.string.tts_engine_voice_count, new Object[]{Integer.valueOf(list.size())}));
            this.languageHint.setText(list.isEmpty() ? getString(R.string.tts_no_voices) : getString(R.string.tts_language_hint));
        }
    }

    private String labelForSavedLanguage(String str) {
        if (str == null || str.isEmpty()) {
            return getString(R.string.tts_language_auto) + " (" + SpeechLanguages.displayLabel(Locale.getDefault()) + ")";
        }
        return SpeechLanguages.displayLabel(SpeechLanguages.localeForTag(str));
    }

    private String keyFor(TextView textView) {
        int id = textView.getId();
        if (id == R.id.chip_theme_system) {
            return AppPreferences.APPEARANCE_SYSTEM;
        }
        if (id == R.id.chip_theme_light) {
            return "light";
        }
        if (id == R.id.chip_theme_dark) {
            return "dark";
        }
        if (id == R.id.chip_font_small) {
            return "small";
        }
        if (id == R.id.chip_font_medium) {
            return "medium";
        }
        if (id == R.id.chip_font_large) {
            return "large";
        }
        if (id == R.id.chip_width_normal) {
            return AppPreferences.WIDTH_NORMAL;
        }
        if (id == R.id.chip_width_wide) {
            return AppPreferences.WIDTH_WIDE;
        }
        if (id == R.id.chip_speed_75) {
            return "0.75";
        }
        if (id == R.id.chip_speed_100) {
            return "1.0";
        }
        if (id == R.id.chip_speed_125) {
            return "1.25";
        }
        if (id == R.id.chip_speed_150) {
            return "1.5";
        }
        return AppPreferences.TTS_LANGUAGE_AUTO;
    }

    private void onChipTapped(TextView textView) {
        int id = textView.getId();
        if (id == R.id.chip_theme_system) {
            this.preferences.setAppearanceMode(AppPreferences.APPEARANCE_SYSTEM);
        } else if (id == R.id.chip_theme_light) {
            this.preferences.setAppearanceMode("light");
        } else if (id == R.id.chip_theme_dark) {
            this.preferences.setAppearanceMode("dark");
        } else if (id == R.id.chip_font_small) {
            this.preferences.setFontSizePreset("small");
        } else if (id == R.id.chip_font_medium) {
            this.preferences.setFontSizePreset("medium");
        } else if (id == R.id.chip_font_large) {
            this.preferences.setFontSizePreset("large");
        } else if (id == R.id.chip_width_normal) {
            this.preferences.setReadingWidth(AppPreferences.WIDTH_NORMAL);
        } else if (id == R.id.chip_width_wide) {
            this.preferences.setReadingWidth(AppPreferences.WIDTH_WIDE);
        } else if (id == R.id.chip_speed_75 || id == R.id.chip_speed_100 || id == R.id.chip_speed_125 || id == R.id.chip_speed_150) {
            this.preferences.setTtsSpeed(speedFor(textView));
        }
        if (id == R.id.chip_theme_system || id == R.id.chip_theme_light || id == R.id.chip_theme_dark) {
            refreshTheme();
        } else {
            paintAll();
        }
    }

    private float speedFor(TextView textView) {
        int id = textView.getId();
        if (id == R.id.chip_speed_75) {
            return 0.75f;
        }
        if (id == R.id.chip_speed_125) {
            return 1.25f;
        }
        return id == R.id.chip_speed_150 ? 1.5f : 1.0f;
    }

    private void handleExport() {
        this.backupExecutor.execute(new Runnable() { // from class: com.offread.app.ui.SettingsActivity.18
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.lambda$handleExport$11();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleExport$11() {
        try {
            String strExportToJson = BackupHelper.exportToJson(this.repository.getAllArticlesForExport());
            File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (externalFilesDir == null) {
                externalFilesDir = getFilesDir();
            }
            final Uri uriBackupUriFor = backupUriFor(BackupHelper.writeBackupToFile(externalFilesDir, strExportToJson).getName());
            this.mainHandler.post(new Runnable() { // from class: com.offread.app.ui.SettingsActivity.2
                @Override // java.lang.Runnable
                public final void run() {
                    SettingsActivity.this.lambda$handleExport$10(uriBackupUriFor);
                }
            });
        } catch (Exception unused) {
            this.mainHandler.post(new Runnable() { // from class: com.offread.app.ui.SettingsActivity.1
                @Override // java.lang.Runnable
                public final void run() {
                    SettingsActivity.this.lambda$handleExport$9();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleExport$9() {
        Toast.makeText(this, R.string.export_failed, 1).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleExport$10(Uri uri) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("application/json");
        intent.putExtra("android.intent.extra.STREAM", uri);
        intent.addFlags(1);
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.pref_export_library)));
            Toast.makeText(this, R.string.export_success, 0).show();
        } catch (Exception unused) {
            Toast.makeText(this, R.string.export_failed, 1).show();
        }
    }

    private Uri backupUriFor(String str) {
        return new Uri.Builder().scheme(OffReadDbHelper.COL_CONTENT).authority(BackupFileProvider.AUTHORITY).appendPath(BackupFileProvider.PATH_SEGMENT).appendPath(str).build();
    }

    private void handleImport() {
        this.backupExecutor.execute(new Runnable() { // from class: com.offread.app.ui.SettingsActivity.15
            @Override // java.lang.Runnable
            public final void run() {
                SettingsActivity.this.lambda$handleImport$15();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleImport$15() {
        File fileNewestBackup = newestBackup();
        if (fileNewestBackup == null) {
            this.mainHandler.post(new Runnable() { // from class: com.offread.app.ui.SettingsActivity.11
                @Override // java.lang.Runnable
                public final void run() {
                    SettingsActivity.this.lambda$handleImport$12();
                }
            });
            return;
        }
        try {
            int i = 0;
            for (Article article : BackupHelper.importFromJson(BackupHelper.readBackupFromFile(fileNewestBackup))) {
                article.setId(0L);
                if (this.repository.findArticleByUrl(article.getUrl()) == null) {
                    this.repository.insertArticle(article);
                    i++;
                }
            }
            final int importedCount = i;
            this.mainHandler.post(new Runnable() { // from class: com.offread.app.ui.SettingsActivity.13
                @Override // java.lang.Runnable
                public final void run() {
                    SettingsActivity.this.lambda$handleImport$14(importedCount);
                }
            });
        } catch (Exception unused) {
            this.mainHandler.post(new Runnable() { // from class: com.offread.app.ui.SettingsActivity.12
                @Override // java.lang.Runnable
                public final void run() {
                    SettingsActivity.this.lambda$handleImport$13();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleImport$12() {
        Toast.makeText(this, R.string.import_failed, 1).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleImport$13() {
        Toast.makeText(this, R.string.import_failed, 1).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleImport$14(int i) {
        Toast.makeText(this, getString(R.string.import_success, new Object[]{Integer.valueOf(i)}), 0).show();
        bindAbout();
    }

    private File newestBackup() {
        File[] fileArrListFiles;
        File[] fileArr = {getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), getFilesDir()};
        File file = null;
        for (int i = 0; i < 2; i++) {
            File file2 = fileArr[i];
            if (file2 != null && file2.isDirectory() && (fileArrListFiles = file2.listFiles(new FileFilter() { // from class: com.offread.app.ui.SettingsActivity.16
                @Override // java.io.FileFilter
                public final boolean accept(File file3) {
                    return SettingsActivity.lambda$newestBackup$16(file3);
                }
            })) != null) {
                Arrays.sort(fileArrListFiles, new Comparator() { // from class: com.offread.app.ui.SettingsActivity.17
                    @Override // java.util.Comparator
                    public final int compare(Object obj, Object obj2) {
                        return SettingsActivity.lambda$newestBackup$17((File) obj, (File) obj2);
                    }
                });
                if (fileArrListFiles.length > 0 && (file == null || fileArrListFiles[0].lastModified() > file.lastModified())) {
                    file = fileArrListFiles[0];
                }
            }
        }
        return file;
    }

    static /* synthetic */ boolean lambda$newestBackup$16(File file) {
        return file.getName().startsWith("offread_backup_") && file.getName().endsWith(".json");
    }

    static /* synthetic */ int lambda$newestBackup$17(File file, File file2) {
        return Long.compare(file2.lastModified(), file.lastModified());
    }

    private void handleRestoreSamples() {
        for (Article article : SampleContentHelper.getSampleArticles()) {
            if (this.repository.findArticleByUrl(article.getUrl()) == null) {
                this.repository.insertArticle(article);
            }
        }
        Toast.makeText(this, R.string.samples_loaded, 0).show();
        bindAbout();
    }

    private void handleClearAll() {
        new AlertDialog.Builder(this, ThemeManager.alertTheme(this)).setTitle(R.string.confirm_clear_title).setMessage(R.string.confirm_clear_message).setNegativeButton(R.string.action_cancel, (DialogInterface.OnClickListener) null).setPositiveButton(R.string.pref_clear_all, new DialogInterface.OnClickListener() { // from class: com.offread.app.ui.SettingsActivity.14
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                SettingsActivity.this.lambda$handleClearAll$18(dialogInterface, i);
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$handleClearAll$18(DialogInterface dialogInterface, int i) {
        this.repository.clearAll();
        Toast.makeText(this, R.string.settings_cleared, 0).show();
        bindAbout();
    }
}
