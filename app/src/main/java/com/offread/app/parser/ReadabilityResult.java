package com.offread.app.parser;

public class ReadabilityResult {
    private final String title;
    private final String author;
    private final String content;
    private final String plainText;
    private final String excerpt;
    private final int wordCount;
    private final int readingTimeMinutes;

    public ReadabilityResult(String title, String author, String content, String plainText,
                             String excerpt, int wordCount, int readingTimeMinutes) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.plainText = plainText;
        this.excerpt = excerpt;
        this.wordCount = wordCount;
        this.readingTimeMinutes = Math.max(1, readingTimeMinutes);
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getContent() { return content; }
    public String getPlainText() { return plainText; }
    public String getExcerpt() { return excerpt; }
    public int getWordCount() { return wordCount; }
    public int getReadingTimeMinutes() { return readingTimeMinutes; }
}
