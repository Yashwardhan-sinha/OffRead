package com.offread.app.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Article implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String title;
    private String author;
    private String url;
    private String domain;
    private String content;
    private String plainText;
    private String excerpt;
    private int wordCount;
    private int readingTimeMinutes;
    private int scrollPosition;
    private int progressPercent;
    private boolean isFavorite;
    private boolean isArchived;
    private String category;
    private String tags;
    private long createdAt;
    private long updatedAt;
    private long lastReadAt;
    private String canonicalUrl;
    private String publishedDate;
    private String imageUrl;

    public Article() {
        this.title = "";
        this.author = "";
        this.url = "";
        this.domain = "";
        this.content = "";
        this.plainText = "";
        this.excerpt = "";
        this.category = "General";
        this.tags = "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.canonicalUrl = "";
        this.publishedDate = "";
        this.imageUrl = "";
    }

    public Article(long id, String title, String author, String url, String domain,
                   String content, String plainText, String excerpt, int wordCount,
                   int readingTimeMinutes, int scrollPosition, int progressPercent,
                   boolean isFavorite, boolean isArchived, String category,
                   String tags, long createdAt, long updatedAt, long lastReadAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.url = url;
        this.domain = domain;
        this.content = content;
        this.plainText = plainText;
        this.excerpt = excerpt;
        this.wordCount = wordCount;
        this.readingTimeMinutes = readingTimeMinutes;
        this.scrollPosition = scrollPosition;
        this.progressPercent = progressPercent;
        this.isFavorite = isFavorite;
        this.isArchived = isArchived;
        this.category = category;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastReadAt = lastReadAt;
        this.canonicalUrl = "";
        this.publishedDate = "";
        this.imageUrl = "";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title == null ? "" : title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author == null ? "" : author; }
    public void setAuthor(String author) { this.author = author; }

    public String getUrl() { return url == null ? "" : url; }
    public void setUrl(String url) { this.url = url; }

    public String getDomain() { return domain == null ? "" : domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getContent() { return content == null ? "" : content; }
    public void setContent(String content) { this.content = content; }

    public String getPlainText() { return plainText == null ? "" : plainText; }
    public void setPlainText(String plainText) { this.plainText = plainText; }

    public String getExcerpt() { return excerpt == null ? "" : excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public int getReadingTimeMinutes() { return readingTimeMinutes; }
    public void setReadingTimeMinutes(int readingTimeMinutes) { this.readingTimeMinutes = readingTimeMinutes; }

    public int getScrollPosition() { return scrollPosition; }
    public void setScrollPosition(int scrollPosition) { this.scrollPosition = scrollPosition; }

    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }

    public String getCategory() { return category == null ? "General" : category; }
    public void setCategory(String category) { this.category = category; }

    public String getTags() { return tags == null ? "" : tags; }
    public void setTags(String tags) { this.tags = tags; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public long getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(long lastReadAt) { this.lastReadAt = lastReadAt; }

    public String getCanonicalUrl() { return canonicalUrl == null ? "" : canonicalUrl; }
    public void setCanonicalUrl(String canonicalUrl) { this.canonicalUrl = canonicalUrl; }

    public String getPublishedDate() { return publishedDate == null ? "" : publishedDate; }
    public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }

    public String getImageUrl() { return imageUrl == null ? "" : imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("author", author);
        obj.put("url", url);
        obj.put("domain", domain);
        obj.put("content", content);
        obj.put("plainText", plainText);
        obj.put("excerpt", excerpt);
        obj.put("wordCount", wordCount);
        obj.put("readingTimeMinutes", readingTimeMinutes);
        obj.put("scrollPosition", scrollPosition);
        obj.put("progressPercent", progressPercent);
        obj.put("isFavorite", isFavorite);
        obj.put("isArchived", isArchived);
        obj.put("category", category);
        obj.put("tags", tags);
        obj.put("createdAt", createdAt);
        obj.put("updatedAt", updatedAt);
        obj.put("lastReadAt", lastReadAt);
        obj.put("canonicalUrl", canonicalUrl);
        obj.put("publishedDate", publishedDate);
        obj.put("imageUrl", imageUrl);
        return obj;
    }

    public static Article fromJson(JSONObject obj) {
        Article a = new Article();
        a.id = obj.optLong("id", 0);
        a.title = obj.optString("title", "");
        a.author = obj.optString("author", "");
        a.url = obj.optString("url", "");
        a.domain = obj.optString("domain", "");
        a.content = obj.optString("content", "");
        a.plainText = obj.optString("plainText", "");
        a.excerpt = obj.optString("excerpt", "");
        a.wordCount = obj.optInt("wordCount", 0);
        a.readingTimeMinutes = obj.optInt("readingTimeMinutes", 1);
        a.scrollPosition = obj.optInt("scrollPosition", 0);
        a.progressPercent = obj.optInt("progressPercent", 0);
        a.isFavorite = obj.optBoolean("isFavorite", false);
        a.isArchived = obj.optBoolean("isArchived", false);
        a.category = obj.optString("category", "General");
        a.tags = obj.optString("tags", "");
        a.createdAt = obj.optLong("createdAt", System.currentTimeMillis());
        a.updatedAt = obj.optLong("updatedAt", System.currentTimeMillis());
        a.lastReadAt = obj.optLong("lastReadAt", 0);
        a.canonicalUrl = obj.optString("canonicalUrl", "");
        a.publishedDate = obj.optString("publishedDate", "");
        a.imageUrl = obj.optString("imageUrl", "");
        return a;
    }
}
