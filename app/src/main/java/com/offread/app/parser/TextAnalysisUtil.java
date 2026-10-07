package com.offread.app.parser;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
            URI uri = new URI(urlString.trim());
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

    public static String resolveUrl(String baseUrl, String relativeUrl) {
        if (relativeUrl == null || relativeUrl.trim().isEmpty()) {
            return "";
        }
        String rel = relativeUrl.trim();
        if (rel.startsWith("data:") || rel.startsWith("javascript:") || rel.startsWith("mailto:")) {
            return "";
        }
        if (rel.startsWith("http://") || rel.startsWith("https://")) {
            return rel;
        }
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return rel;
        }

        String base = baseUrl.trim();
        if (rel.startsWith("//")) {
            if (base.startsWith("https://")) {
                return "https:" + rel;
            } else if (base.startsWith("http://")) {
                return "http:" + rel;
            }
            return "https:" + rel;
        }

        try {
            URI baseUri = new URI(base);
            URI resolved = baseUri.resolve(rel);
            return resolved.toString();
        } catch (Exception e) {
            if (rel.startsWith("/")) {
                int schemeEnd = base.indexOf("://");
                if (schemeEnd != -1) {
                    int slash = base.indexOf('/', schemeEnd + 3);
                    if (slash != -1) {
                        return base.substring(0, slash) + rel;
                    }
                    return base + rel;
                }
            }
            return base + (base.endsWith("/") ? "" : "/") + rel;
        }
    }

    public static String cleanTitle(String title, String siteName) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }
        String clean = decodeHtmlEntities(title.trim());
        clean = clean.replaceAll("\\s+", " ").trim();

        // Check delimiters: " | ", " - ", " — ", " – ", " :: ", " • ", " » ", " · "
        String[] delimiters = new String[]{" | ", " — ", " – ", " - ", " :: ", " • ", " » ", " · "};
        for (String delim : delimiters) {
            int idx = clean.lastIndexOf(delim);
            if (idx > 0) {
                String candidate = clean.substring(0, idx).trim();
                String suffix = clean.substring(idx + delim.length()).trim();

                // If suffix matches siteName or looks like branding, strip it
                if (siteName != null && !siteName.isEmpty() &&
                        (suffix.equalsIgnoreCase(siteName) || suffix.toLowerCase(Locale.ROOT).contains(siteName.toLowerCase(Locale.ROOT)))) {
                    return candidate;
                }
                // If candidate is a good sentence or headline (> 15 chars), prefer it over a short suffix
                if (candidate.length() >= 12 && suffix.length() < 30) {
                    clean = candidate;
                    break;
                }
            }
        }
        return clean;
    }

    public static String cleanAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            return "";
        }
        String clean = decodeHtmlEntities(author.trim());
        clean = clean.replaceAll("(?i)^(by|written by|author:|posted by|words by)\\s+", "");
        clean = clean.replaceAll("\\s+", " ").trim();
        return clean;
    }

    public static String stripMarkdown(String markdown) {
        if (markdown == null || markdown.isEmpty()) return "";
        String s = markdown;
        // Strip code blocks
        s = s.replaceAll("```[a-zA-Z]*\\n[\\s\\S]*?```", "");
        // Strip inline code
        s = s.replaceAll("`([^`]+)`", "$1");
        // Strip image links ![alt](url) -> ""
        s = s.replaceAll("!\\[[^\\]]*\\]\\([^)]+\\)", "");
        // Strip markdown links [text](url) -> text
        s = s.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");
        // Strip headings (#, ##, ###)
        s = s.replaceAll("(?m)^#{1,6}\\s*", "");
        // Strip blockquotes
        s = s.replaceAll("(?m)^>\\s*", "");
        // Strip bold & italic
        s = s.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
        s = s.replaceAll("\\*([^*]+)\\*", "$1");
        s = s.replaceAll("__([^_]+)__", "$1");
        s = s.replaceAll("_([^_]+)_", "$1");
        // Strip horizontal rules
        s = s.replaceAll("(?m)^[-*_]{3,}\\s*$", "");
        // Clean multi-newlines
        s = s.replaceAll("\n{3,}", "\n\n");
        return s.trim();
    }

    public static String decodeHtmlEntities(String text) {
        if (text == null || text.isEmpty()) return "";
        String s = text.replace("&nbsp;", " ")
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
                .replace("&rsquo;", "’")
                .replace("&laquo;", "«")
                .replace("&raquo;", "»")
                .replace("&copy;", "©")
                .replace("&reg;", "®")
                .replace("&trade;", "™")
                .replace("&bull;", "•")
                .replace("&cent;", "¢")
                .replace("&pound;", "£")
                .replace("&yen;", "¥")
                .replace("&euro;", "€");

        // Decimal numeric entities &#NNN;
        if (s.contains("&#")) {
            Pattern decPattern = Pattern.compile("&#([0-9]{1,7});");
            Matcher decMatcher = decPattern.matcher(s);
            StringBuilder sb = new StringBuilder();
            while (decMatcher.find()) {
                try {
                    int codePoint = Integer.parseInt(decMatcher.group(1));
                    if (Character.isValidCodePoint(codePoint)) {
                        decMatcher.appendReplacement(sb, Matcher.quoteReplacement(new String(Character.toChars(codePoint))));
                    } else {
                        decMatcher.appendReplacement(sb, "");
                    }
                } catch (Exception e) {
                    decMatcher.appendReplacement(sb, decMatcher.group(0));
                }
            }
            decMatcher.appendTail(sb);
            s = sb.toString();

            // Hex numeric entities &#xNNN;
            Pattern hexPattern = Pattern.compile("(?i)&#x([0-9a-f]{1,6});");
            Matcher hexMatcher = hexPattern.matcher(s);
            StringBuilder sbHex = new StringBuilder();
            while (hexMatcher.find()) {
                try {
                    int codePoint = Integer.parseInt(hexMatcher.group(1), 16);
                    if (Character.isValidCodePoint(codePoint)) {
                        hexMatcher.appendReplacement(sbHex, Matcher.quoteReplacement(new String(Character.toChars(codePoint))));
                    } else {
                        hexMatcher.appendReplacement(sbHex, "");
                    }
                } catch (Exception e) {
                    hexMatcher.appendReplacement(sbHex, hexMatcher.group(0));
                }
            }
            hexMatcher.appendTail(sbHex);
            s = sb.toString();
        }

        return s;
    }
}
