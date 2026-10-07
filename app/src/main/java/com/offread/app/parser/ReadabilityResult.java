package com.offread.app.parser;

public class ReadabilityResult {
    private final String title;
    private final String author;
    private final String content;
    private final String plainText;
    private final String excerpt;
    private final int wordCount;
    private final int readingTimeMinutes;
    private final String publishedDate;
    private final String leadImageUrl;
    private final String siteName;
    private final String canonicalUrl;
    private final String language;

    public ReadabilityResult(String title, String author, String content, String plainText,
                             String excerpt, int wordCount, int readingTimeMinutes) {
        this(title, author, content, plainText, excerpt, wordCount, readingTimeMinutes,
                "", "", "", "", "");
    }

    public ReadabilityResult(String title, String author, String content, String plainText,
                             String excerpt, int wordCount, int readingTimeMinutes,
                             String publishedDate, String leadImageUrl, String siteName,
                             String canonicalUrl, String language) {
        this.title = title != null ? title.trim() : "Untitled";
        this.author = author != null ? author.trim() : "";
        this.content = content != null ? content.trim() : "";
        this.plainText = plainText != null ? plainText.trim() : "";
        this.excerpt = excerpt != null ? excerpt.trim() : "";
        this.wordCount = Math.max(0, wordCount);
        this.readingTimeMinutes = Math.max(1, readingTimeMinutes);
        this.publishedDate = publishedDate != null ? publishedDate.trim() : "";
        this.leadImageUrl = leadImageUrl != null ? leadImageUrl.trim() : "";
        this.siteName = siteName != null ? siteName.trim() : "";
        this.canonicalUrl = canonicalUrl != null ? canonicalUrl.trim() : "";
        this.language = language != null ? language.trim() : "";
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getContent() { return content; }
    public String getPlainText() { return plainText; }
    public String getExcerpt() { return excerpt; }
    public int getWordCount() { return wordCount; }
    public int getReadingTimeMinutes() { return readingTimeMinutes; }
    public String getPublishedDate() { return publishedDate; }
    public String getLeadImageUrl() { return leadImageUrl; }
    public String getSiteName() { return siteName; }
    public String getCanonicalUrl() { return canonicalUrl; }
    public String getLanguage() { return language; }
}
