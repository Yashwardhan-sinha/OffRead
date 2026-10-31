package com.offread.app.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public final class ReaderMetaFormatter {
    static final int MAX_TAGS = 8;
    static final String SEPARATOR = "  •  ";

    private ReaderMetaFormatter() {
    }

    public static String join(String... strArr) {
        StringBuilder sb = new StringBuilder();
        if (strArr == null) {
            return "";
        }
        for (String str : strArr) {
            if (str != null) {
                String strTrim = str.trim();
                if (!strTrim.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append(SEPARATOR);
                    }
                    sb.append(strTrim);
                }
            }
        }
        return sb.toString();
    }

    public static List<String> splitTags(String str) {
        ArrayList arrayList = new ArrayList();
        if (str != null && !str.trim().isEmpty()) {
            LinkedHashSet linkedHashSet = new LinkedHashSet();
            for (String str2 : str.split(",")) {
                String strTrim = str2.trim();
                if (!strTrim.isEmpty() && linkedHashSet.add(strTrim.toLowerCase(Locale.ROOT))) {
                    arrayList.add(strTrim);
                    if (arrayList.size() >= 8) {
                        break;
                    }
                }
            }
        }
        return arrayList;
    }
}
