package com.offread.app.parser;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextAnalysisUtil {

    private static final int WORDS_PER_MINUTE = 200;

    public static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] tokens = text.trim().split("\\s+");
        int count = 0;
        for (String t : tokens) {
            if (!t.isEmpty()) count++;
        }
        return count;
    }

    public static int calculateReadingTimeMinutes(int wordCount) {
        if (wordCount <= 0) return 1;
        int minutes = (wordCount + WORDS_PER_MINUTE - 1) / WORDS_PER_MINUTE;
        return Math.max(1, minutes);
    }

    public static String createExcerpt(String text, int maxChars) {
        if (text == null) return "";
        String clean = text.replaceAll("\\s+", " ").trim();
        if (clean.length() <= maxChars) {
            return clean;
        }
        int cut = clean.lastIndexOf(' ', maxChars);
        if (cut <= 0) cut = maxChars;
        return clean.substring(0, cut).trim() + "…";
    }

    public static int calculateOrpIndex(String word) {
        if (word == null || word.isEmpty()) return 0;
        int len = word.length();
        if (len <= 1) return 0;
        if (len <= 5) return 1;
        if (len <= 9) return 2;
        if (len <= 13) return 3;
        return 4;
    }

    public static List<String> splitSentences(String text) {
        List<String> sentences = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return sentences;
        }

        // Split by punctuation followed by whitespace or line break
        Pattern pattern = Pattern.compile("([^.!?\\n]+[.!?]?)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String s = matcher.group(1).trim();
            if (!s.isEmpty()) {
                sentences.add(s);
            }
        }

        if (sentences.isEmpty()) {
            sentences.add(text.trim());
        }
        return sentences;
    }

    public static String extractDomain(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return "Note";
        }
        try {
            URI uri = new URI(urlString);
            String host = uri.getHost();
            if (host != null) {
                if (host.startsWith("www.")) {
                    return host.substring(4);
                }
                return host;
            }
        } catch (Exception ignored) {
        }
        return "Web";
    }

    public static String decodeHtmlEntities(String text) {
        if (text == null) return "";
        return text.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&mdash;", "—")
                .replace("&ndash;", "–")
                .replace("&hellip;", "…")
                .replace("&ldquo;", "\"")
                .replace("&rdquo;", "\"")
                .replace("&lsquo;", "‘")
                .replace("&rsquo;", "’");
    }
}
