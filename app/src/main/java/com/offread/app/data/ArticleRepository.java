package com.offread.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArticleRepository {
    public enum FilterType {
        ALL,
        UNREAD,
        FAVORITES,
        ARCHIVE
    }

    public enum SortOrder {
        NEWEST,
        OLDEST,
        READING_TIME,
        TITLE
    }

    private static ArticleRepository sInstance;
    private final OffReadDbHelper dbHelper;

    public static synchronized ArticleRepository getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ArticleRepository(context.getApplicationContext());
        }
        return sInstance;
    }

    private ArticleRepository(Context context) {
        this.dbHelper = new OffReadDbHelper(context);
    }

    public long insertArticle(Article article) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(OffReadDbHelper.COL_TITLE, article.getTitle());
        cv.put(OffReadDbHelper.COL_AUTHOR, article.getAuthor());
        cv.put(OffReadDbHelper.COL_URL, article.getUrl());
        cv.put(OffReadDbHelper.COL_DOMAIN, article.getDomain());
        cv.put(OffReadDbHelper.COL_CONTENT, article.getContent());
        cv.put(OffReadDbHelper.COL_PLAIN_TEXT, article.getPlainText());
        cv.put(OffReadDbHelper.COL_EXCERPT, article.getExcerpt());
        cv.put(OffReadDbHelper.COL_WORD_COUNT, article.getWordCount());
        cv.put(OffReadDbHelper.COL_READING_TIME, article.getReadingTimeMinutes());
        cv.put(OffReadDbHelper.COL_SCROLL_POS, article.getScrollPosition());
        cv.put(OffReadDbHelper.COL_PROGRESS, article.getProgressPercent());
        cv.put(OffReadDbHelper.COL_IS_FAVORITE, article.isFavorite() ? 1 : 0);
        cv.put(OffReadDbHelper.COL_IS_ARCHIVED, article.isArchived() ? 1 : 0);
        cv.put(OffReadDbHelper.COL_CATEGORY, article.getCategory());
        cv.put(OffReadDbHelper.COL_TAGS, article.getTags());
        cv.put(OffReadDbHelper.COL_CREATED_AT, article.getCreatedAt());
        cv.put(OffReadDbHelper.COL_UPDATED_AT, article.getUpdatedAt());
        cv.put(OffReadDbHelper.COL_LAST_READ_AT, article.getLastReadAt());
        cv.put(OffReadDbHelper.COL_CANONICAL_URL, article.getCanonicalUrl());
        cv.put(OffReadDbHelper.COL_PUBLISHED_DATE, article.getPublishedDate());
        cv.put(OffReadDbHelper.COL_IMAGE_URL, article.getImageUrl());

        long id = db.insert(OffReadDbHelper.TABLE_ARTICLES, null, cv);
        article.setId(id);
        return id;
    }

    public boolean updateArticle(Article article) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(OffReadDbHelper.COL_TITLE, article.getTitle());
        cv.put(OffReadDbHelper.COL_AUTHOR, article.getAuthor());
        cv.put(OffReadDbHelper.COL_URL, article.getUrl());
        cv.put(OffReadDbHelper.COL_DOMAIN, article.getDomain());
        cv.put(OffReadDbHelper.COL_CONTENT, article.getContent());
        cv.put(OffReadDbHelper.COL_PLAIN_TEXT, article.getPlainText());
        cv.put(OffReadDbHelper.COL_EXCERPT, article.getExcerpt());
        cv.put(OffReadDbHelper.COL_WORD_COUNT, article.getWordCount());
        cv.put(OffReadDbHelper.COL_READING_TIME, article.getReadingTimeMinutes());
        cv.put(OffReadDbHelper.COL_SCROLL_POS, article.getScrollPosition());
        cv.put(OffReadDbHelper.COL_PROGRESS, article.getProgressPercent());
        cv.put(OffReadDbHelper.COL_IS_FAVORITE, article.isFavorite() ? 1 : 0);
        cv.put(OffReadDbHelper.COL_IS_ARCHIVED, article.isArchived() ? 1 : 0);
        cv.put(OffReadDbHelper.COL_CATEGORY, article.getCategory());
        cv.put(OffReadDbHelper.COL_TAGS, article.getTags());
        cv.put(OffReadDbHelper.COL_UPDATED_AT, System.currentTimeMillis());
        cv.put(OffReadDbHelper.COL_LAST_READ_AT, article.getLastReadAt());
        cv.put(OffReadDbHelper.COL_CANONICAL_URL, article.getCanonicalUrl());
        cv.put(OffReadDbHelper.COL_PUBLISHED_DATE, article.getPublishedDate());
        cv.put(OffReadDbHelper.COL_IMAGE_URL, article.getImageUrl());

        int count = db.update(OffReadDbHelper.TABLE_ARTICLES, cv,
                OffReadDbHelper.COL_ID + " = ?", new String[]{String.valueOf(article.getId())});
        return count > 0;
    }

    public Article getArticle(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(OffReadDbHelper.TABLE_ARTICLES, null,
                OffReadDbHelper.COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
        if (c != null && c.moveToFirst()) {
            Article article = mapCursorToArticle(c);
            c.close();
            return article;
        }
        if (c != null) c.close();
        return null;
    }

    public boolean deleteArticle(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(OffReadDbHelper.TABLE_ARTICLES,
                OffReadDbHelper.COL_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public void clearAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(OffReadDbHelper.TABLE_HIGHLIGHTS, null, null);
        db.delete(OffReadDbHelper.TABLE_READING_LOGS, null, null);
        db.delete(OffReadDbHelper.TABLE_ARTICLES, null, null);
    }

    public boolean toggleFavorite(long id) {
        Article a = getArticle(id);
        if (a != null) {
            a.setFavorite(!a.isFavorite());
            updateArticle(a);
            return a.isFavorite();
        }
        return false;
    }

    public boolean toggleArchive(long id) {
        Article a = getArticle(id);
        if (a != null) {
            a.setArchived(!a.isArchived());
            updateArticle(a);
            return a.isArchived();
        }
        return false;
    }

    public void updateReadingProgress(long id, int scrollY, int progressPercent) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(OffReadDbHelper.COL_SCROLL_POS, scrollY);
        cv.put(OffReadDbHelper.COL_PROGRESS, Math.max(0, Math.min(100, progressPercent)));
        cv.put(OffReadDbHelper.COL_LAST_READ_AT, System.currentTimeMillis());
        if (progressPercent >= 95) {
            cv.put(OffReadDbHelper.COL_IS_ARCHIVED, 1);
        }
        db.update(OffReadDbHelper.TABLE_ARTICLES, cv,
                OffReadDbHelper.COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void markAsCompleted(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(OffReadDbHelper.COL_PROGRESS, 100);
        cv.put(OffReadDbHelper.COL_IS_ARCHIVED, 1);
        cv.put(OffReadDbHelper.COL_LAST_READ_AT, System.currentTimeMillis());
        db.update(OffReadDbHelper.TABLE_ARTICLES, cv,
                OffReadDbHelper.COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<Article> getArticles(FilterType filter, String searchQuery, String tagFilter, SortOrder sort) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> whereClauses = new ArrayList<>();
        List<String> whereArgs = new ArrayList<>();

        if (filter == FilterType.UNREAD) {
            whereClauses.add(OffReadDbHelper.COL_IS_ARCHIVED + " = 0");
        } else if (filter == FilterType.FAVORITES) {
            whereClauses.add(OffReadDbHelper.COL_IS_FAVORITE + " = 1");
        } else if (filter == FilterType.ARCHIVE) {
            whereClauses.add(OffReadDbHelper.COL_IS_ARCHIVED + " = 1");
        }

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String query = "%" + searchQuery.trim() + "%";
            whereClauses.add("(" + OffReadDbHelper.COL_TITLE + " LIKE ? OR " +
                    OffReadDbHelper.COL_PLAIN_TEXT + " LIKE ? OR " +
                    OffReadDbHelper.COL_AUTHOR + " LIKE ? OR " +
                    OffReadDbHelper.COL_DOMAIN + " LIKE ? OR " +
                    OffReadDbHelper.COL_TAGS + " LIKE ?)");
            whereArgs.add(query);
            whereArgs.add(query);
            whereArgs.add(query);
            whereArgs.add(query);
            whereArgs.add(query);
        }

        if (tagFilter != null && !tagFilter.trim().isEmpty()) {
            whereClauses.add(OffReadDbHelper.COL_TAGS + " LIKE ?");
            whereArgs.add("%" + tagFilter.trim() + "%");
        }

        String selection = null;
        if (!whereClauses.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < whereClauses.size(); i++) {
                if (i > 0) sb.append(" AND ");
                sb.append(whereClauses.get(i));
            }
            selection = sb.toString();
        }

        String orderBy = OffReadDbHelper.COL_CREATED_AT + " DESC";
        if (sort == SortOrder.OLDEST) {
            orderBy = OffReadDbHelper.COL_CREATED_AT + " ASC";
        } else if (sort == SortOrder.READING_TIME) {
            orderBy = OffReadDbHelper.COL_READING_TIME + " ASC";
        } else if (sort == SortOrder.TITLE) {
            orderBy = OffReadDbHelper.COL_TITLE + " COLLATE NOCASE ASC";
        }

        Cursor c = db.query(OffReadDbHelper.TABLE_ARTICLES, null, selection,
                whereArgs.toArray(new String[0]), null, null, orderBy);

        List<Article> result = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                result.add(mapCursorToArticle(c));
            } while (c.moveToNext());
            c.close();
        } else if (c != null) {
            c.close();
        }
        return result;
    }

    public List<Article> getAllArticlesForExport() {
        return getArticles(FilterType.ALL, null, null, SortOrder.NEWEST);
    }

    public Set<String> getAllTags() {
        List<Article> articles = getAllArticlesForExport();
        Set<String> tags = new HashSet<>();
        for (Article a : articles) {
            String t = a.getTags();
            if (t != null && !t.trim().isEmpty()) {
                String[] parts = t.split(",");
                for (String p : parts) {
                    String clean = p.trim();
                    if (!clean.isEmpty()) {
                        tags.add(clean);
                    }
                }
            }
        }
        return tags;
    }

    public void recordReadingSession(long articleId, int secondsRead, int wordsRead) {
        if (secondsRead <= 0 && wordsRead <= 0) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(OffReadDbHelper.COL_LOG_ARTICLE_ID, articleId);
        cv.put(OffReadDbHelper.COL_LOG_SECONDS, secondsRead);
        cv.put(OffReadDbHelper.COL_LOG_WORDS, wordsRead);
        cv.put(OffReadDbHelper.COL_LOG_CREATED_AT, System.currentTimeMillis());
        db.insert(OffReadDbHelper.TABLE_READING_LOGS, null, cv);
    }

    public ReadingStats getReadingStats() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int totalArticles = 0;
        int finishedArticles = 0;

        Cursor c1 = db.rawQuery("SELECT COUNT(*), SUM(CASE WHEN " + OffReadDbHelper.COL_PROGRESS +
                " >= 95 OR " + OffReadDbHelper.COL_IS_ARCHIVED + " = 1 THEN 1 ELSE 0 END) FROM " +
                OffReadDbHelper.TABLE_ARTICLES, null);
        if (c1 != null && c1.moveToFirst()) {
            totalArticles = c1.getInt(0);
            finishedArticles = c1.getInt(1);
            c1.close();
        } else if (c1 != null) {
            c1.close();
        }

        int totalSeconds = 0;
        int totalWords = 0;
        Cursor c2 = db.rawQuery("SELECT SUM(" + OffReadDbHelper.COL_LOG_SECONDS + "), SUM(" +
                OffReadDbHelper.COL_LOG_WORDS + ") FROM " + OffReadDbHelper.TABLE_READING_LOGS, null);
        if (c2 != null && c2.moveToFirst()) {
            totalSeconds = c2.getInt(0);
            totalWords = c2.getInt(1);
            c2.close();
        } else if (c2 != null) {
            c2.close();
        }

        // Calculate active streak days from reading logs
        int streak = 0;
        Cursor c3 = db.rawQuery("SELECT DISTINCT strftime('%Y-%m-%d', datetime(" +
                OffReadDbHelper.COL_LOG_CREATED_AT + "/1000, 'unixepoch', 'localtime')) AS day FROM " +
                OffReadDbHelper.TABLE_READING_LOGS + " ORDER BY day DESC LIMIT 30", null);
        if (c3 != null) {
            streak = c3.getCount();
            if (streak == 0 && totalArticles > 0) {
                streak = 1;
            }
            c3.close();
        }

        return new ReadingStats(totalArticles, finishedArticles, totalWords, totalSeconds, Math.max(1, streak));
    }

    public long addHighlight(Highlight highlight) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(OffReadDbHelper.COL_HL_ARTICLE_ID, highlight.getArticleId());
        cv.put(OffReadDbHelper.COL_HL_TEXT, highlight.getText());
        cv.put(OffReadDbHelper.COL_HL_NOTE, highlight.getNote());
        cv.put(OffReadDbHelper.COL_HL_COLOR, highlight.getColor());
        cv.put(OffReadDbHelper.COL_HL_CREATED_AT, highlight.getCreatedAt());
        long id = db.insert(OffReadDbHelper.TABLE_HIGHLIGHTS, null, cv);
        highlight.setId(id);
        return id;
    }

    public List<Highlight> getHighlightsForArticle(long articleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(OffReadDbHelper.TABLE_HIGHLIGHTS, null,
                OffReadDbHelper.COL_HL_ARTICLE_ID + " = ?", new String[]{String.valueOf(articleId)},
                null, null, OffReadDbHelper.COL_HL_CREATED_AT + " ASC");
        List<Highlight> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                Highlight h = new Highlight(
                        c.getLong(c.getColumnIndexOrThrow(OffReadDbHelper.COL_HL_ID)),
                        c.getLong(c.getColumnIndexOrThrow(OffReadDbHelper.COL_HL_ARTICLE_ID)),
                        c.getString(c.getColumnIndexOrThrow(OffReadDbHelper.COL_HL_TEXT)),
                        c.getString(c.getColumnIndexOrThrow(OffReadDbHelper.COL_HL_NOTE)),
                        c.getString(c.getColumnIndexOrThrow(OffReadDbHelper.COL_HL_COLOR)),
                        c.getLong(c.getColumnIndexOrThrow(OffReadDbHelper.COL_HL_CREATED_AT))
                );
                list.add(h);
            } while (c.moveToNext());
            c.close();
        } else if (c != null) {
            c.close();
        }
        return list;
    }

    private Article mapCursorToArticle(Cursor c) {
        Article a = new Article();
        a.setId(c.getLong(c.getColumnIndexOrThrow(OffReadDbHelper.COL_ID)));
        a.setTitle(c.getString(c.getColumnIndexOrThrow(OffReadDbHelper.COL_TITLE)));
        a.setAuthor(c.getString(c.getColumnIndexOrThrow(OffReadDbHelper.COL_AUTHOR)));
        a.setUrl(c.getString(c.getColumnIndexOrThrow(OffReadDbHelper.COL_URL)));
        a.setDomain(c.getString(c.getColumnIndexOrThrow(OffReadDbHelper.COL_DOMAIN)));
        a.setContent(c.getString(c.getColumnIndexOrThrow(OffReadDbHelper.COL_CONTENT)));
        a.setPlainText(c.getString(c.getColumnIndexOrThrow(OffReadDbHelper.COL_PLAIN_TEXT)));
        a.setExcerpt(c.getString(c.getColumnIndexOrThrow(OffReadDbHelper.COL_EXCERPT)));
        a.setWordCount(c.getInt(c.getColumnIndexOrThrow(OffReadDbHelper.COL_WORD_COUNT)));
        a.setReadingTimeMinutes(c.getInt(c.getColumnIndexOrThrow(OffReadDbHelper.COL_READING_TIME)));
        a.setScrollPosition(c.getInt(c.getColumnIndexOrThrow(OffReadDbHelper.COL_SCROLL_POS)));
        a.setProgressPercent(c.getInt(c.getColumnIndexOrThrow(OffReadDbHelper.COL_PROGRESS)));
        a.setFavorite(c.getInt(c.getColumnIndexOrThrow(OffReadDbHelper.COL_IS_FAVORITE)) == 1);
        a.setArchived(c.getInt(c.getColumnIndexOrThrow(OffReadDbHelper.COL_IS_ARCHIVED)) == 1);
        a.setCategory(c.getString(c.getColumnIndexOrThrow(OffReadDbHelper.COL_CATEGORY)));
        a.setTags(c.getString(c.getColumnIndexOrThrow(OffReadDbHelper.COL_TAGS)));
        a.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(OffReadDbHelper.COL_CREATED_AT)));
        a.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(OffReadDbHelper.COL_UPDATED_AT)));
        a.setLastReadAt(c.getLong(c.getColumnIndexOrThrow(OffReadDbHelper.COL_LAST_READ_AT)));

        int canonicalIdx = c.getColumnIndex(OffReadDbHelper.COL_CANONICAL_URL);
        if (canonicalIdx != -1) a.setCanonicalUrl(c.getString(canonicalIdx));

        int pubDateIdx = c.getColumnIndex(OffReadDbHelper.COL_PUBLISHED_DATE);
        if (pubDateIdx != -1) a.setPublishedDate(c.getString(pubDateIdx));

        int imgUrlIdx = c.getColumnIndex(OffReadDbHelper.COL_IMAGE_URL);
        if (imgUrlIdx != -1) a.setImageUrl(c.getString(imgUrlIdx));

        return a;
    }

    public Article getArticleByUrl(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(OffReadDbHelper.TABLE_ARTICLES, null,
                OffReadDbHelper.COL_URL + " = ? OR " + OffReadDbHelper.COL_CANONICAL_URL + " = ?",
                new String[]{url.trim(), url.trim()}, null, null, null, "1");
        if (c != null && c.moveToFirst()) {
            Article a = mapCursorToArticle(c);
            c.close();
            return a;
        }
        if (c != null) c.close();
        return null;
    }
}
