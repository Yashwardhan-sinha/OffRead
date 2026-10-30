package com.offread.app.data;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class Article implements Serializable {
    private static final long serialVersionUID = 1;
    private String author;
    private String canonicalUrl;
    private String category;
    private String content;
    private long createdAt;
    private String domain;
    private String excerpt;
    private long id;
    private String imageUrl;
    private boolean isArchived;
    private boolean isFavorite;
    private String language;
    private long lastReadAt;
    private String plainText;
    private int progressPercent;
    private String publishedDate;
    private int readingTimeMinutes;
    private int scrollPosition;
    private String tags;
    private String title;
    private long updatedAt;
    private String url;
    private int wordCount;

    public Article() {
        this.title = AppPreferences.TTS_LANGUAGE_AUTO;
        this.author = AppPreferences.TTS_LANGUAGE_AUTO;
        this.url = AppPreferences.TTS_LANGUAGE_AUTO;
        this.domain = AppPreferences.TTS_LANGUAGE_AUTO;
        this.content = AppPreferences.TTS_LANGUAGE_AUTO;
        this.plainText = AppPreferences.TTS_LANGUAGE_AUTO;
        this.excerpt = AppPreferences.TTS_LANGUAGE_AUTO;
        this.category = "General";
        this.tags = AppPreferences.TTS_LANGUAGE_AUTO;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.canonicalUrl = AppPreferences.TTS_LANGUAGE_AUTO;
        this.publishedDate = AppPreferences.TTS_LANGUAGE_AUTO;
        this.imageUrl = AppPreferences.TTS_LANGUAGE_AUTO;
        this.language = AppPreferences.TTS_LANGUAGE_AUTO;
    }

    public Article(long j, String str, String str2, String str3, String str4, String str5, String str6, String str7, int i, int i2, int i3, int i4, boolean z, boolean z2, String str8, String str9, long j2, long j3, long j4) {
        this.id = j;
        this.title = str;
        this.author = str2;
        this.url = str3;
        this.domain = str4;
        this.content = str5;
        this.plainText = str6;
        this.excerpt = str7;
        this.wordCount = i;
        this.readingTimeMinutes = i2;
        this.scrollPosition = i3;
        this.progressPercent = i4;
        this.isFavorite = z;
        this.isArchived = z2;
        this.category = str8;
        this.tags = str9;
        this.createdAt = j2;
        this.updatedAt = j3;
        this.lastReadAt = j4;
        this.canonicalUrl = AppPreferences.TTS_LANGUAGE_AUTO;
        this.publishedDate = AppPreferences.TTS_LANGUAGE_AUTO;
        this.imageUrl = AppPreferences.TTS_LANGUAGE_AUTO;
        this.language = AppPreferences.TTS_LANGUAGE_AUTO;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long j) {
        this.id = j;
    }

    public String getTitle() {
        String str = this.title;
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public String getAuthor() {
        String str = this.author;
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
    }

    public void setAuthor(String str) {
        this.author = str;
    }

    public String getUrl() {
        String str = this.url;
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public String getDomain() {
        String str = this.domain;
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
    }

    public void setDomain(String str) {
        this.domain = str;
    }

    public String getContent() {
        String str = this.content;
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
    }

    public void setContent(String str) {
        this.content = str;
    }

    public String getPlainText() {
        String str = this.plainText;
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
    }

    public void setPlainText(String str) {
        this.plainText = str;
    }

    public String getExcerpt() {
        String str = this.excerpt;
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
    }

    public void setExcerpt(String str) {
        this.excerpt = str;
    }

    public int getWordCount() {
        return this.wordCount;
    }

    public void setWordCount(int i) {
        this.wordCount = i;
    }

    public int getReadingTimeMinutes() {
        return this.readingTimeMinutes;
    }

    public void setReadingTimeMinutes(int i) {
        this.readingTimeMinutes = i;
    }

    public int getScrollPosition() {
        return this.scrollPosition;
    }

    public void setScrollPosition(int i) {
        this.scrollPosition = i;
    }

    public int getProgressPercent() {
        return this.progressPercent;
    }

    public void setProgressPercent(int i) {
        this.progressPercent = i;
    }

    public boolean isFavorite() {
        return this.isFavorite;
    }

    public void setFavorite(boolean z) {
        this.isFavorite = z;
    }

    public boolean isArchived() {
        return this.isArchived;
    }

    public void setArchived(boolean z) {
        this.isArchived = z;
    }

    public String getCategory() {
        String str = this.category;
        return str == null ? "General" : str;
    }

    public void setCategory(String str) {
        this.category = str;
    }

    public String getTags() {
        String str = this.tags;
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
    }

    public void setTags(String str) {
        this.tags = str;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(long j) {
        this.createdAt = j;
    }

    public long getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(long j) {
        this.updatedAt = j;
    }

    public long getLastReadAt() {
        return this.lastReadAt;
    }

    public void setLastReadAt(long j) {
        this.lastReadAt = j;
    }

    public String getCanonicalUrl() {
        String str = this.canonicalUrl;
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
    }

    public void setCanonicalUrl(String str) {
        this.canonicalUrl = str;
    }

    public static String identityUrl(String str, String str2) {
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str.trim();
    }

    public String getPublishedDate() {
        String str = this.publishedDate;
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
    }

    public void setPublishedDate(String str) {
        this.publishedDate = str;
    }

    public String getImageUrl() {
        String str = this.imageUrl;
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
    }

    public void setImageUrl(String str) {
        this.imageUrl = str;
    }

    public String getLanguage() {
        String str = this.language;
        return str == null ? AppPreferences.TTS_LANGUAGE_AUTO : str;
    }

    public void setLanguage(String str) {
        this.language = str;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("id", this.id);
        jSONObject.put(OffReadDbHelper.COL_TITLE, this.title);
        jSONObject.put(OffReadDbHelper.COL_AUTHOR, this.author);
        jSONObject.put(OffReadDbHelper.COL_URL, this.url);
        jSONObject.put(OffReadDbHelper.COL_DOMAIN, this.domain);
        jSONObject.put(OffReadDbHelper.COL_CONTENT, this.content);
        jSONObject.put("plainText", this.plainText);
        jSONObject.put(OffReadDbHelper.COL_EXCERPT, this.excerpt);
        jSONObject.put("wordCount", this.wordCount);
        jSONObject.put("readingTimeMinutes", this.readingTimeMinutes);
        jSONObject.put("scrollPosition", this.scrollPosition);
        jSONObject.put("progressPercent", this.progressPercent);
        jSONObject.put("isFavorite", this.isFavorite);
        jSONObject.put("isArchived", this.isArchived);
        jSONObject.put(OffReadDbHelper.COL_CATEGORY, this.category);
        jSONObject.put(OffReadDbHelper.COL_TAGS, this.tags);
        jSONObject.put("createdAt", this.createdAt);
        jSONObject.put("updatedAt", this.updatedAt);
        jSONObject.put("lastReadAt", this.lastReadAt);
        jSONObject.put("canonicalUrl", this.canonicalUrl);
        jSONObject.put("publishedDate", this.publishedDate);
        jSONObject.put("imageUrl", this.imageUrl);
        jSONObject.put("language", this.language);
        return jSONObject;
    }

    public static Article fromJson(JSONObject jSONObject) {
        Article article = new Article();
        article.id = jSONObject.optLong("id", 0L);
        article.title = jSONObject.optString(OffReadDbHelper.COL_TITLE, AppPreferences.TTS_LANGUAGE_AUTO);
        article.author = jSONObject.optString(OffReadDbHelper.COL_AUTHOR, AppPreferences.TTS_LANGUAGE_AUTO);
        article.url = jSONObject.optString(OffReadDbHelper.COL_URL, AppPreferences.TTS_LANGUAGE_AUTO);
        article.domain = jSONObject.optString(OffReadDbHelper.COL_DOMAIN, AppPreferences.TTS_LANGUAGE_AUTO);
        article.content = jSONObject.optString(OffReadDbHelper.COL_CONTENT, AppPreferences.TTS_LANGUAGE_AUTO);
        article.plainText = jSONObject.optString("plainText", AppPreferences.TTS_LANGUAGE_AUTO);
        article.excerpt = jSONObject.optString(OffReadDbHelper.COL_EXCERPT, AppPreferences.TTS_LANGUAGE_AUTO);
        article.wordCount = jSONObject.optInt("wordCount", 0);
        article.readingTimeMinutes = jSONObject.optInt("readingTimeMinutes", 1);
        article.scrollPosition = jSONObject.optInt("scrollPosition", 0);
        article.progressPercent = jSONObject.optInt("progressPercent", 0);
        article.isFavorite = jSONObject.optBoolean("isFavorite", false);
        article.isArchived = jSONObject.optBoolean("isArchived", false);
        article.category = jSONObject.optString(OffReadDbHelper.COL_CATEGORY, "General");
        article.tags = jSONObject.optString(OffReadDbHelper.COL_TAGS, AppPreferences.TTS_LANGUAGE_AUTO);
        article.createdAt = jSONObject.optLong("createdAt", System.currentTimeMillis());
        article.updatedAt = jSONObject.optLong("updatedAt", System.currentTimeMillis());
        article.lastReadAt = jSONObject.optLong("lastReadAt", 0L);
        article.canonicalUrl = jSONObject.optString("canonicalUrl", AppPreferences.TTS_LANGUAGE_AUTO);
        article.publishedDate = jSONObject.optString("publishedDate", AppPreferences.TTS_LANGUAGE_AUTO);
        article.imageUrl = jSONObject.optString("imageUrl", AppPreferences.TTS_LANGUAGE_AUTO);
        article.language = jSONObject.optString("language", AppPreferences.TTS_LANGUAGE_AUTO);
        return article;
    }
}
