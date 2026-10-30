package com.offread.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import com.offread.app.util.ThemeManager;
import com.offread.app.util.UIHelper;

/* JADX INFO: loaded from: classes.dex */
public class ThemedActivity extends Activity {
    public static final int SECTION_BOOKMARKS = 1;
    public static final int SECTION_HOME = 0;
    public static final int SECTION_SETTINGS = 2;
    private String appliedAppearanceMode;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        this.appliedAppearanceMode = readAppearanceMode();
        ThemeManager.applyTo(this);
        super.onCreate(bundle);
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if (readAppearanceMode().equals(this.appliedAppearanceMode)) {
            return;
        }
        refreshTheme();
    }

    private String readAppearanceMode() {
        return new AppPreferences(this).getAppearanceMode();
    }

    protected void refreshTheme() {
        this.appliedAppearanceMode = readAppearanceMode();
        ThemeManager.applyTo(this);
        recreate();
    }

    protected int themeColor(int i) {
        TypedValue typedValue = new TypedValue();
        if (!getTheme().resolveAttribute(i, typedValue, true)) {
            return 0;
        }
        if (typedValue.type >= 16 && typedValue.type <= 31) {
            return typedValue.data;
        }
        if (typedValue.resourceId != 0) {
            return getResources().getColor(typedValue.resourceId);
        }
        return 0;
    }

    protected float dimen(int i) {
        return getResources().getDimension(i);
    }

    protected int dpToPx(float f) {
        return (int) TypedValue.applyDimension(1, f, getResources().getDisplayMetrics());
    }

    protected void applySystemBars() {
        applySystemBars(themeColor(R.attr.colorBackground));
    }

    protected void applySystemBars(int i) {
        UIHelper.applySystemBars(this, themeColor(R.attr.colorBackground), i, !(ThemeManager.nightMode(this) == 1));
    }

    protected void goToSection(int i) {
        Class cls;
        if (i == 1) {
            cls = BookmarksActivity.class;
        } else if (i == 2) {
            cls = SettingsActivity.class;
        } else {
            cls = MainActivity.class;
        }
        Intent intent = new Intent(this, (Class<?>) cls);
        intent.addFlags(603979776);
        startActivity(intent);
        finish();
    }
}
