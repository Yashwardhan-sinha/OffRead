package com.offread.app.parser;

import com.offread.app.data.AppPreferences;
import java.util.Locale;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public final class TextNormalizer {
    private static final Pattern MULTI_SPACE = Pattern.compile("[ \\t\\u00A0\\u2007\\u202F]+");
    private static final Pattern BLANK_RUN = Pattern.compile("(\\n[ \\t]*){2,}");
    private static final Pattern EMPTY_HEADING = Pattern.compile("(?m)^#{1,6}[ \\t]*$");
    private static final Pattern EMPTY_QUOTE = Pattern.compile("(?m)^>[ \\t]*$");
    private static final Pattern STRAY_FENCE = Pattern.compile("(?m)^```[a-zA-Z0-9]*[ \\t]*$");
    private static final Pattern EMPTY_BULLET = Pattern.compile("(?m)^[\\u2022\\-*+][ \\t]+$");
    private static final Pattern LONE_MARKERS = Pattern.compile("(?m)^[ \\t]*(\\u2022|\\*|-+|_+|=+|\\|+|>+|\\)+|\\(+|\\[\\s*\\]|[.\\u00B7\\u2026]+)[ \\t]*$");

    private TextNormalizer() {
    }

    public static String normalizeArticle(String str) {
        if (str == null || str.isEmpty()) {
            return AppPreferences.TTS_LANGUAGE_AUTO;
        }
        return collapseFenceBlanks(dropPlaceholderLines(BLANK_RUN.matcher(LONE_MARKERS.matcher(EMPTY_BULLET.matcher(EMPTY_QUOTE.matcher(EMPTY_HEADING.matcher(MULTI_SPACE.matcher(scrubInvisible(str).replace("\r\n", "\n").replace('\r', '\n')).replaceAll(" ").replaceAll("[ \\t]+\\n", "\n").replaceAll("\\n[ \\t]+", "\n")).replaceAll(AppPreferences.TTS_LANGUAGE_AUTO)).replaceAll(AppPreferences.TTS_LANGUAGE_AUTO)).replaceAll(AppPreferences.TTS_LANGUAGE_AUTO)).replaceAll(AppPreferences.TTS_LANGUAGE_AUTO)).replaceAll("\n\n").replaceAll("\n{3,}", "\n\n"))).replaceAll("\n{3,}", "\n\n").trim();
    }

    public static String normalizeInline(String str) {
        if (str == null || str.isEmpty()) {
            return AppPreferences.TTS_LANGUAGE_AUTO;
        }
        String strTrim = MULTI_SPACE.matcher(scrubInvisible(str)).replaceAll(" ").replaceAll("[ \\t]*\\n[ \\t]*", " ").trim();
        return AdFilter.isAdPlaceholderText(strTrim) ? AppPreferences.TTS_LANGUAGE_AUTO : strTrim;
    }

    public static String dropPlaceholderLines(String str) {
        if (str == null || str.isEmpty()) {
            return AppPreferences.TTS_LANGUAGE_AUTO;
        }
        String[] strArrSplit = str.split("\n", -1);
        StringBuilder sb = new StringBuilder(str.length());
        boolean z = false;
        for (String str2 : strArrSplit) {
            String strTrim = str2.trim();
            if (strTrim.startsWith("```")) {
                z = !z;
                appendLine(sb, str2);
            } else if (z) {
                appendLine(sb, str2);
            } else if (strTrim.isEmpty()) {
                appendLine(sb, str2);
            } else {
                String strStripBlockMarkers = stripBlockMarkers(strTrim);
                if (!strStripBlockMarkers.isEmpty() && !AdFilter.isAdPlaceholderText(strStripBlockMarkers) && !AdFilter.isSeparatorOrSpinner(strStripBlockMarkers)) {
                    appendLine(sb, str2);
                }
            }
        }
        return sb.toString();
    }

    private static void appendLine(StringBuilder sb, String str) {
        if (str.trim().isEmpty() && sb.length() > 0 && endsWithBlank(sb)) {
            return;
        }
        if (str.trim().isEmpty() && sb.length() == 0) {
            return;
        }
        if (sb.length() > 0) {
            sb.append('\n');
        }
        sb.append(str);
    }

    private static boolean endsWithBlank(StringBuilder sb) {
        for (int length = sb.length() - 1; length >= 0; length--) {
            if (sb.charAt(length) != '\n') {
                return false;
            }
            if (length == sb.length() - 1) {
                return true;
            }
        }
        return true;
    }

    private static String stripBlockMarkers(String str) {
        return str.replaceAll("^\\#+[ \\t]+", AppPreferences.TTS_LANGUAGE_AUTO).replaceAll("^>+[ \\t]*", AppPreferences.TTS_LANGUAGE_AUTO).replaceAll("^[\\u2022\\-*+][ \\t]+", AppPreferences.TTS_LANGUAGE_AUTO).replaceAll("^[-=*~_\\s]+$", AppPreferences.TTS_LANGUAGE_AUTO).trim();
    }

    private static String collapseFenceBlanks(String str) {
        return str.replaceAll("```[ \\t]*\\n[ \\t]*\\n", "```\n\n").replaceAll("[ \\t]*\\n[ \\t]*```", "\n```");
    }

    public static String scrubInvisible(String str) {
        if (str == null) {
            return AppPreferences.TTS_LANGUAGE_AUTO;
        }
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            if (cCharAt == '\n' || cCharAt == '\t') {
                if (cCharAt == '\t') {
                    cCharAt = ' ';
                }
                sb.append(cCharAt);
            } else if (cCharAt == 160 || cCharAt == 8199 || cCharAt == 8239) {
                sb.append(' ');
            } else if (cCharAt != 8203 && cCharAt != 8204 && cCharAt != 8205 && cCharAt != 65279 && cCharAt != 173 && cCharAt != 8288 && cCharAt != 6158 && cCharAt >= ' ' && (cCharAt < 57344 || cCharAt > 63743)) {
                sb.append(cCharAt);
            }
        }
        return sb.toString();
    }

    public static boolean isReadableContentEmpty(String str) {
        return str == null || TextAnalysisUtil.stripMarkdown(str).replaceAll("[\\s\\p{Punct}]", AppPreferences.TTS_LANGUAGE_AUTO).toLowerCase(Locale.ROOT).length() < 3;
    }
}
