package com.offread.app.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class ArticleExtractor {

    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Mobile Safari/537.36";
    private static final int TIMEOUT_MS = 15000;

    public static ReadabilityResult fetchFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setInstanceFollowRedirects(true);

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            throw new Exception("HTTP error code: " + responseCode);
        }

        InputStream in = conn.getInputStream();
        String encoding = conn.getContentEncoding();
        if ("gzip".equalsIgnoreCase(encoding)) {
            in = new GZIPInputStream(in);
        }

        StringBuilder htmlBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                htmlBuilder.append(line).append("\n");
            }
        } finally {
            conn.disconnect();
        }

        return extractFromHtml(htmlBuilder.toString(), urlString);
    }

    public static ReadabilityResult extractFromHtml(String rawHtml, String sourceUrl) {
        if (rawHtml == null || rawHtml.trim().isEmpty()) {
            return new ReadabilityResult("Untitled", "", "", "", "", 0, 1);
        }

        // 1. Extract Title
        String title = extractMeta(rawHtml, "og:title");
        if (title.isEmpty()) {
            title = extractMeta(rawHtml, "twitter:title");
        }
        if (title.isEmpty()) {
            title = extractRegex(rawHtml, "<title[^>]*>([^<]+)</title>");
            if (!title.isEmpty()) {
                int pipe = title.indexOf(" | ");
                if (pipe > 0) title = title.substring(0, pipe);
                int dash = title.indexOf(" - ");
                if (dash > 0) title = title.substring(0, dash);
            }
        }
        if (title.isEmpty()) {
            title = extractRegex(rawHtml, "<h1[^>]*>([^<]+)</h1>");
        }
        if (title.isEmpty()) {
            title = TextAnalysisUtil.extractDomain(sourceUrl);
        }
        title = TextAnalysisUtil.decodeHtmlEntities(title.trim());

        // 2. Extract Author
        String author = extractMeta(rawHtml, "author");
        if (author.isEmpty()) {
            author = extractMeta(rawHtml, "article:author");
        }
        if (author.isEmpty()) {
            author = extractRegex(rawHtml, "class=[\"'][^\"']*author[^\"']*[\"'][^>]*>([^<]+)<");
        }
        author = TextAnalysisUtil.decodeHtmlEntities(author.trim());

        // 3. Remove non-content blocks
        String cleaned = rawHtml;
        cleaned = removeTagBlock(cleaned, "script");
        cleaned = removeTagBlock(cleaned, "style");
        cleaned = removeTagBlock(cleaned, "noscript");
        cleaned = removeTagBlock(cleaned, "nav");
        cleaned = removeTagBlock(cleaned, "footer");
        cleaned = removeTagBlock(cleaned, "header");
        cleaned = removeTagBlock(cleaned, "aside");
        cleaned = removeTagBlock(cleaned, "svg");
        cleaned = removeTagBlock(cleaned, "iframe");
        cleaned = removeTagBlock(cleaned, "form");
        cleaned = cleaned.replaceAll("(?s)<!--.*?-->", "");

        // 4. Locate primary content block
        String mainSection = extractTagBlock(cleaned, "article");
        if (mainSection.isEmpty()) {
            mainSection = extractTagBlock(cleaned, "main");
        }
        if (mainSection.isEmpty()) {
            // Check for class matching post/article/entry/story
            Pattern p = Pattern.compile("(?is)<(?:div|section)[^>]+class=[\"'][^\"']*(?:article-body|post-content|entry-content|story-body|article_body|content-body)[^\"']*[\"'][^>]*>(.*?)</(?:div|section)>");
            Matcher m = p.matcher(cleaned);
            if (m.find()) {
                mainSection = m.group(1);
            }
        }
        if (mainSection.isEmpty()) {
            mainSection = extractTagBlock(cleaned, "body");
        }
        if (mainSection.isEmpty()) {
            mainSection = cleaned;
        }

        // 5. Structure and clean textual paragraphs
        String formatted = formatReadableText(mainSection);
        String plainText = stripAllTags(formatted).trim();
        int wordCount = TextAnalysisUtil.countWords(plainText);
        int readTime = TextAnalysisUtil.calculateReadingTimeMinutes(wordCount);
        String excerpt = TextAnalysisUtil.createExcerpt(plainText, 160);

        return new ReadabilityResult(title, author, formatted, plainText, excerpt, wordCount, readTime);
    }

    public static ReadabilityResult processUserText(String title, String author, String rawText) {
        String safeTitle = (title != null && !title.trim().isEmpty()) ? title.trim() : "Untitled Note";
        String safeAuthor = (author != null) ? author.trim() : "";
        String safeText = (rawText != null) ? rawText.trim() : "";

        // Format raw text into clean paragraphs
        String[] paragraphs = safeText.split("\n\\s*\n");
        StringBuilder formatted = new StringBuilder();
        for (String p : paragraphs) {
            String clean = p.trim();
            if (!clean.isEmpty()) {
                formatted.append(clean).append("\n\n");
            }
        }

        String plainText = safeText;
        int wordCount = TextAnalysisUtil.countWords(plainText);
        int readTime = TextAnalysisUtil.calculateReadingTimeMinutes(wordCount);
        String excerpt = TextAnalysisUtil.createExcerpt(plainText, 160);

        return new ReadabilityResult(safeTitle, safeAuthor, formatted.toString().trim(), plainText, excerpt, wordCount, readTime);
    }

    private static String extractMeta(String html, String nameOrProperty) {
        Pattern p = Pattern.compile("(?i)<meta\\s+[^>]*(?:name|property)=[\"']" + Pattern.quote(nameOrProperty) + "[\"']\\s+content=[\"']([^\"']*)[\"']");
        Matcher m = p.matcher(html);
        if (m.find()) {
            return m.group(1);
        }
        // Try reverse attribute order (content before name/property)
        Pattern p2 = Pattern.compile("(?i)<meta\\s+[^>]*content=[\"']([^\"']*)[\"']\\s+[^>]*(?:name|property)=[\"']" + Pattern.quote(nameOrProperty) + "[\"']");
        Matcher m2 = p2.matcher(html);
        if (m2.find()) {
            return m2.group(1);
        }
        return "";
    }

    private static String extractRegex(String source, String regex) {
        Pattern p = Pattern.compile("(?is)" + regex);
        Matcher m = p.matcher(source);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private static String removeTagBlock(String html, String tagName) {
        return html.replaceAll("(?is)<" + tagName + "[^>]*>.*?</" + tagName + ">", " ");
    }

    private static String extractTagBlock(String html, String tagName) {
        Pattern p = Pattern.compile("(?is)<" + tagName + "[^>]*>(.*?)</" + tagName + ">");
        Matcher m = p.matcher(html);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private static String formatReadableText(String html) {
        // Replace paragraph and heading endings with spacing
        String s = html.replaceAll("(?is)<(p|div|section|article)[^>]*>", "\n\n")
                .replaceAll("(?is)</(p|div|section|article)>", "\n")
                .replaceAll("(?is)<(h[1-6])[^>]*>", "\n\n### ")
                .replaceAll("(?is)</(h[1-6])>", "\n\n")
                .replaceAll("(?is)<br\\s*/?>", "\n")
                .replaceAll("(?is)<li[^>]*>", "\n• ")
                .replaceAll("(?is)</li>", "")
                .replaceAll("(?is)<blockquote[^>]*>", "\n\n> ")
                .replaceAll("(?is)</blockquote>", "\n\n");

        s = stripAllTags(s);
        s = TextAnalysisUtil.decodeHtmlEntities(s);

        // Collapse multiple blank lines
        s = s.replaceAll("\n{3,}", "\n\n").trim();
        return s;
    }

    private static String stripAllTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]+>", "");
    }
}
