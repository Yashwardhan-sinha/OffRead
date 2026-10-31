package com.offread.app.parser;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public class TextAnalysisUtil {
    private static final int WORDS_PER_MINUTE = 200;

    public static int countWords(String str) {
        if (str == null || str.trim().isEmpty()) {
            return 0;
        }
        int i = 0;
        for (String str2 : str.trim().split("\\s+")) {
            if (!str2.isEmpty()) {
                i++;
            }
        }
        return i;
    }

    public static int calculateReadingTimeMinutes(int i) {
        if (i <= 0) {
            return 1;
        }
        return Math.max(1, (i + 199) / WORDS_PER_MINUTE);
    }

    public static String createExcerpt(String str, int i) {
        if (str == null) {
            return "";
        }
        String strTrim = str.replaceAll("\\s+", " ").trim();
        if (strTrim.length() <= i) {
            return strTrim;
        }
        int iLastIndexOf = strTrim.lastIndexOf(32, i);
        if (iLastIndexOf > 0) {
            i = iLastIndexOf;
        }
        return strTrim.substring(0, i).trim() + "…";
    }

    public static int calculateOrpIndex(String str) {
        int length;
        if (str == null || str.isEmpty() || (length = str.length()) <= 1) {
            return 0;
        }
        if (length <= 5) {
            return 1;
        }
        if (length <= 9) {
            return 2;
        }
        return length <= 13 ? 3 : 4;
    }

    public static List<String> splitSentences(String str) {
        ArrayList arrayList = new ArrayList();
        if (str != null && !str.trim().isEmpty()) {
            Matcher matcher = Pattern.compile("([^.!?\\n]+[.!?]?)").matcher(str);
            while (matcher.find()) {
                String strTrim = matcher.group(1).trim();
                if (!strTrim.isEmpty()) {
                    arrayList.add(strTrim);
                }
            }
            if (arrayList.isEmpty()) {
                arrayList.add(str.trim());
            }
        }
        return arrayList;
    }

    public static String extractDomain(String str) {
        if (str == null || str.trim().isEmpty()) {
            return "Note";
        }
        try {
            String host = new URI(str.trim()).getHost();
            if (host != null) {
                return host.startsWith("www.") ? host.substring(4) : host;
            }
            return "Web";
        } catch (Exception unused) {
            return "Web";
        }
    }

    public static String resolveUrl(String str, String str2) {
        int iIndexOf;
        String str3 = "";
        if (str2 != null && !str2.trim().isEmpty()) {
            String strTrim = str2.trim();
            if (!strTrim.startsWith("data:") && !strTrim.startsWith("javascript:") && !strTrim.startsWith("mailto:")) {
                if (strTrim.startsWith("http://") || strTrim.startsWith("https://") || str == null || str.trim().isEmpty()) {
                    return strTrim;
                }
                String strTrim2 = str.trim();
                if (strTrim.startsWith("//")) {
                    if (strTrim2.startsWith("https://")) {
                        return "https:" + strTrim;
                    }
                    if (strTrim2.startsWith("http://")) {
                        return "http:" + strTrim;
                    }
                    return "https:" + strTrim;
                }
                try {
                    return new URI(strTrim2).resolve(strTrim).toString();
                } catch (Exception unused) {
                    if (strTrim.startsWith("/") && (iIndexOf = strTrim2.indexOf("://")) != -1) {
                        int iIndexOf2 = strTrim2.indexOf(47, iIndexOf + 3);
                        if (iIndexOf2 != -1) {
                            return strTrim2.substring(0, iIndexOf2) + strTrim;
                        }
                        return strTrim2 + strTrim;
                    }
                    if (!strTrim2.endsWith("/")) {
                        str3 = "/";
                    }
                    return strTrim2 + str3 + strTrim;
                }
            }
        }
        return "";
    }

    public static String cleanTitle(String str, String str2) {
        if (str == null || str.trim().isEmpty()) {
            return "";
        }
        String strTrim = decodeHtmlEntities(str.trim()).replaceAll("\\s+", " ").trim();
        String[] strArr = {" | ", " — ", " – ", " - ", " :: ", " • ", " » ", " · "};
        for (int i = 0; i < 8; i++) {
            String str3 = strArr[i];
            int iLastIndexOf = strTrim.lastIndexOf(str3);
            if (iLastIndexOf > 0) {
                String strTrim2 = strTrim.substring(0, iLastIndexOf).trim();
                String strTrim3 = strTrim.substring(iLastIndexOf + str3.length()).trim();
                if ((str2 != null && !str2.isEmpty() && (strTrim3.equalsIgnoreCase(str2) || strTrim3.toLowerCase(Locale.ROOT).contains(str2.toLowerCase(Locale.ROOT)))) || (strTrim2.length() >= 12 && strTrim3.length() < 30)) {
                    return strTrim2;
                }
            }
        }
        return strTrim;
    }

    public static String cleanAuthor(String str) {
        if (str == null || str.trim().isEmpty()) {
            return "";
        }
        return decodeHtmlEntities(str.trim()).replaceAll("(?i)^(by|written by|author:|posted by|words by)\\s+", "").replaceAll("\\s+", " ").trim();
    }

    public static String stripMarkdown(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        return str.replaceAll("```[a-zA-Z]*\\n[\\s\\S]*?```", "").replaceAll("`([^`]+)`", "$1").replaceAll("!\\[[^\\]]*\\]\\([^)]+\\)", "").replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1").replaceAll("(?m)^#{1,6}\\s*", "").replaceAll("(?m)^>\\s*", "").replaceAll("\\*\\*([^*]+)\\*\\*", "$1").replaceAll("\\*([^*]+)\\*", "$1").replaceAll("__([^_]+)__", "$1").replaceAll("_([^_]+)_", "$1").replaceAll("(?m)^[-*_]{3,}\\s*$", "").replaceAll("\n{3,}", "\n\n").trim();
    }

    public static String decodeHtmlEntities(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        String strReplace = str.replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"").replace("&apos;", "'").replace("&#39;", "'").replace("&lt;", "<").replace("&gt;", ">").replace("&mdash;", "—").replace("&ndash;", "–").replace("&hellip;", "…").replace("&ldquo;", "\"").replace("&rdquo;", "\"").replace("&lsquo;", "‘").replace("&rsquo;", "’").replace("&laquo;", "«").replace("&raquo;", "»").replace("&copy;", "©").replace("&reg;", "®").replace("&trade;", "™").replace("&bull;", "•").replace("&cent;", "¢").replace("&pound;", "£").replace("&yen;", "¥").replace("&euro;", "€");
        if (!strReplace.contains("&#")) {
            return strReplace;
        }
        Matcher matcher = Pattern.compile("&#([0-9]{1,7});").matcher(strReplace);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            try {
                int i = Integer.parseInt(matcher.group(1));
                if (Character.isValidCodePoint(i)) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(new String(Character.toChars(i))));
                } else {
                    matcher.appendReplacement(sb, "");
                }
            } catch (Exception unused) {
                matcher.appendReplacement(sb, matcher.group(0));
            }
        }
        matcher.appendTail(sb);
        Matcher matcher2 = Pattern.compile("(?i)&#x([0-9a-f]{1,6});").matcher(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        while (matcher2.find()) {
            try {
                int i2 = Integer.parseInt(matcher2.group(1), 16);
                if (Character.isValidCodePoint(i2)) {
                    matcher2.appendReplacement(sb2, Matcher.quoteReplacement(new String(Character.toChars(i2))));
                } else {
                    matcher2.appendReplacement(sb2, "");
                }
            } catch (Exception unused2) {
                matcher2.appendReplacement(sb2, matcher2.group(0));
            }
        }
        matcher2.appendTail(sb2);
        return sb.toString();
    }
}
