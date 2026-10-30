package com.offread.app.ui;

import android.content.Context;
import com.offread.app.R;
import com.offread.app.data.AppPreferences;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public final class ReadingDateFormatter {
    private ReadingDateFormatter() {
    }

    public static String format(Context context, long j) {
        if (context == null || j <= 0) {
            return AppPreferences.TTS_LANGUAGE_AUTO;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(j);
        Calendar calendar2 = Calendar.getInstance();
        if (sameDay(calendar, calendar2)) {
            return timeFormat().format(new Date(j));
        }
        Calendar calendar3 = Calendar.getInstance();
        calendar3.add(6, -1);
        if (sameDay(calendar, calendar3)) {
            return context.getString(R.string.date_yesterday);
        }
        if (calendar.get(1) == calendar2.get(1)) {
            return new SimpleDateFormat("d MMM", Locale.getDefault()).format(new Date(j));
        }
        return new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date(j));
    }

    private static DateFormat timeFormat() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    private static boolean sameDay(Calendar calendar, Calendar calendar2) {
        return calendar.get(1) == calendar2.get(1) && calendar.get(6) == calendar2.get(6);
    }
}
