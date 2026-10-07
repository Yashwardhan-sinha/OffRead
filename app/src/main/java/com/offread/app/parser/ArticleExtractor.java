package com.offread.app.parser;

public class ArticleExtractor {

    public static ReadabilityResult fetchFromUrl(String urlString) throws Exception {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        NetworkFetcher.FetchResult fetchResult = NetworkFetcher.fetch(urlString.trim());
        return extractFromHtml(fetchResult.html, fetchResult.finalUrl);
    }

    public static ReadabilityResult extractFromHtml(String rawHtml, String sourceUrl) {
        if (rawHtml == null || rawHtml.trim().isEmpty()) {
            String domain = TextAnalysisUtil.extractDomain(sourceUrl);
            return new ReadabilityResult("Untitled", "", "", "", "", 0, 1,
                    "", "", domain, sourceUrl != null ? sourceUrl : "", "");
        }

        // 1. Build DOM Tree
        HtmlNode root = HtmlParser.parse(rawHtml);

        // 2. Extract Metadata (OpenGraph, Twitter Cards, Dublin Core, JSON-LD, Schema)
        MetadataExtractor.ArticleMetadata meta = MetadataExtractor.extract(root, sourceUrl);

        // 3. Extract Main Article Body & Remove Boilerplate / Noise
        ReadabilityEngine.ExtractionResult body = ReadabilityEngine.extractContent(root, sourceUrl, meta.title);

        String markdown = body.markdown;
        String plainText = body.plainText;

        // Fallback to JSON-LD articleBody if DOM extraction yielded empty text
        if (plainText.length() < 50 && meta.jsonLdBody != null && !meta.jsonLdBody.trim().isEmpty()) {
            markdown = meta.jsonLdBody.trim();
            plainText = meta.jsonLdBody.trim();
        }

        // 4. Word count, Reading time, and Excerpt
        int wordCount = TextAnalysisUtil.countWords(plainText);
        int readTime = TextAnalysisUtil.calculateReadingTimeMinutes(wordCount);

        String excerpt = meta.description;
        if (excerpt == null || excerpt.trim().isEmpty()) {
            excerpt = TextAnalysisUtil.createExcerpt(plainText, 160);
        } else {
            excerpt = TextAnalysisUtil.decodeHtmlEntities(excerpt.trim());
        }

        String safeTitle = (meta.title != null && !meta.title.isEmpty()) ?
                meta.title : TextAnalysisUtil.extractDomain(sourceUrl);

        return new ReadabilityResult(
                safeTitle,
                meta.author,
                markdown,
                plainText,
                excerpt,
                wordCount,
                readTime,
                meta.publishedDate,
                meta.leadImageUrl,
                meta.siteName,
                meta.canonicalUrl,
                meta.language
        );
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

        return new ReadabilityResult(
                safeTitle,
                safeAuthor,
                formatted.toString().trim(),
                plainText,
                excerpt,
                wordCount,
                readTime
        );
    }
}
