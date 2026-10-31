package com.offread.app.util;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import com.offread.app.R;
import com.offread.app.data.AppPreferences;

/* JADX INFO: loaded from: classes.dex */
public final class ThemeManager {
    public static final int MODE_DARK = 1;
    public static final int MODE_LIGHT = 0;

    private ThemeManager() {
    }

    public static boolean isNightRequested(Context context) {
        String appearanceMode = new AppPreferences(context).getAppearanceMode();
        if ("dark".equals(appearanceMode)) {
            return true;
        }
        return !"light".equals(appearanceMode) && (context.getResources().getConfiguration().uiMode & 48) == 32;
    }

    public static int nightMode(Context context) {
        return isNightRequested(context) ? 1 : 0;
    }

    public static int activityTheme(Context context) {
        return isNightRequested(context) ? R.style.Theme_OffRead_Dark : R.style.Theme_OffRead_Light;
    }

    public static int dialogTheme(Context context) {
        return isNightRequested(context) ? R.style.Theme_OffRead_Dialog_Dark : R.style.Theme_OffRead_Dialog_Light;
    }

    public static int alertTheme(Context context) {
        return isNightRequested(context) ? R.style.Theme_OffRead_Alert_Dark : R.style.Theme_OffRead_Alert_Light;
    }

    public static void applyTo(Activity activity) {
        activity.setTheme(activityTheme(activity));
    }

    public static int color(Activity activity, int i) {
        TypedValue typedValue = new TypedValue();
        if (activity == null || !activity.getTheme().resolveAttribute(i, typedValue, true)) {
            return 0;
        }
        if (typedValue.type >= 16 && typedValue.type <= 31) {
            return typedValue.data;
        }
        if (typedValue.resourceId != 0) {
            return activity.getResources().getColor(typedValue.resourceId);
        }
        return 0;
    }

    public static float dp(Context context, int i) {
        return context.getResources().getDimension(i);
    }
}
