package com.offread.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public class ArticleRepository {
    private static ArticleRepository sInstance;
    private final OffReadDbHelper dbHelper;

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
        SQLiteDatabase writableDatabase = this.dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(OffReadDbHelper.COL_TITLE, article.getTitle());
        contentValues.put(OffReadDbHelper.COL_AUTHOR, article.getAuthor());
        contentValues.put(OffReadDbHelper.COL_URL, article.getUrl());
        contentValues.put(OffReadDbHelper.COL_DOMAIN, article.getDomain());
        contentValues.put(OffReadDbHelper.COL_CONTENT, article.getContent());
        contentValues.put(OffReadDbHelper.COL_PLAIN_TEXT, article.getPlainText());
        contentValues.put(OffReadDbHelper.COL_EXCERPT, article.getExcerpt());
        contentValues.put(OffReadDbHelper.COL_WORD_COUNT, Integer.valueOf(article.getWordCount()));
        contentValues.put(OffReadDbHelper.COL_READING_TIME, Integer.valueOf(article.getReadingTimeMinutes()));
        contentValues.put(OffReadDbHelper.COL_SCROLL_POS, Integer.valueOf(article.getScrollPosition()));
        contentValues.put(OffReadDbHelper.COL_PROGRESS, Integer.valueOf(article.getProgressPercent()));
        contentValues.put(OffReadDbHelper.COL_IS_FAVORITE, Integer.valueOf(article.isFavorite() ? 1 : 0));
        contentValues.put(OffReadDbHelper.COL_IS_ARCHIVED, Integer.valueOf(article.isArchived() ? 1 : 0));
        contentValues.put(OffReadDbHelper.COL_CATEGORY, article.getCategory());
        contentValues.put(OffReadDbHelper.COL_TAGS, article.getTags());
        contentValues.put("created_at", Long.valueOf(article.getCreatedAt()));
        contentValues.put(OffReadDbHelper.COL_UPDATED_AT, Long.valueOf(article.getUpdatedAt()));
        contentValues.put(OffReadDbHelper.COL_LAST_READ_AT, Long.valueOf(article.getLastReadAt()));
        contentValues.put(OffReadDbHelper.COL_CANONICAL_URL, article.getCanonicalUrl());
        contentValues.put(OffReadDbHelper.COL_PUBLISHED_DATE, article.getPublishedDate());
        contentValues.put(OffReadDbHelper.COL_IMAGE_URL, article.getImageUrl());
        contentValues.put(OffReadDbHelper.COL_LANGUAGE, article.getLanguage());
        long jInsert = writableDatabase.insert(OffReadDbHelper.TABLE_ARTICLES, null, contentValues);
        article.setId(jInsert);
        return jInsert;
    }

    public boolean updateArticle(Article article) {
        SQLiteDatabase writableDatabase = this.dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(OffReadDbHelper.COL_TITLE, article.getTitle());
        contentValues.put(OffReadDbHelper.COL_AUTHOR, article.getAuthor());
        contentValues.put(OffReadDbHelper.COL_URL, article.getUrl());
        contentValues.put(OffReadDbHelper.COL_DOMAIN, article.getDomain());
        contentValues.put(OffReadDbHelper.COL_CONTENT, article.getContent());
        contentValues.put(OffReadDbHelper.COL_PLAIN_TEXT, article.getPlainText());
        contentValues.put(OffReadDbHelper.COL_EXCERPT, article.getExcerpt());
        contentValues.put(OffReadDbHelper.COL_WORD_COUNT, Integer.valueOf(article.getWordCount()));
        contentValues.put(OffReadDbHelper.COL_READING_TIME, Integer.valueOf(article.getReadingTimeMinutes()));
        contentValues.put(OffReadDbHelper.COL_SCROLL_POS, Integer.valueOf(article.getScrollPosition()));
        contentValues.put(OffReadDbHelper.COL_PROGRESS, Integer.valueOf(article.getProgressPercent()));
        contentValues.put(OffReadDbHelper.COL_IS_FAVORITE, Integer.valueOf(article.isFavorite() ? 1 : 0));
        contentValues.put(OffReadDbHelper.COL_IS_ARCHIVED, Integer.valueOf(article.isArchived() ? 1 : 0));
        contentValues.put(OffReadDbHelper.COL_CATEGORY, article.getCategory());
        contentValues.put(OffReadDbHelper.COL_TAGS, article.getTags());
        contentValues.put(OffReadDbHelper.COL_UPDATED_AT, Long.valueOf(System.currentTimeMillis()));
        contentValues.put(OffReadDbHelper.COL_LAST_READ_AT, Long.valueOf(article.getLastReadAt()));
        contentValues.put(OffReadDbHelper.COL_CANONICAL_URL, article.getCanonicalUrl());
        contentValues.put(OffReadDbHelper.COL_PUBLISHED_DATE, article.getPublishedDate());
        contentValues.put(OffReadDbHelper.COL_IMAGE_URL, article.getImageUrl());
        contentValues.put(OffReadDbHelper.COL_LANGUAGE, article.getLanguage());
        return writableDatabase.update(OffReadDbHelper.TABLE_ARTICLES, contentValues, "_id = ?", new String[]{String.valueOf(article.getId())}) > 0;
    }

    public Article getArticle(long j) {
        Cursor cursorQuery = this.dbHelper.getReadableDatabase().query(OffReadDbHelper.TABLE_ARTICLES, null, "_id = ?", new String[]{String.valueOf(j)}, null, null, null);
        if (cursorQuery != null && cursorQuery.moveToFirst()) {
            Article articleMapCursorToArticle = mapCursorToArticle(cursorQuery);
            cursorQuery.close();
            return articleMapCursorToArticle;
        }
        if (cursorQuery == null) {
            return null;
        }
        cursorQuery.close();
        return null;
    }

    public boolean deleteArticle(long j) {
        SQLiteDatabase writableDatabase = this.dbHelper.getWritableDatabase();
        String[] strArr = {String.valueOf(j)};
        writableDatabase.beginTransaction();
        try {
            writableDatabase.delete(OffReadDbHelper.TABLE_HIGHLIGHTS, "article_id = ?", strArr);
            writableDatabase.delete(OffReadDbHelper.TABLE_READING_LOGS, "article_id = ?", strArr);
            int iDelete = writableDatabase.delete(OffReadDbHelper.TABLE_ARTICLES, "_id = ?", strArr);
            writableDatabase.setTransactionSuccessful();
            return iDelete > 0;
        } finally {
            writableDatabase.endTransaction();
        }
    }

    public void clearAll() {
        SQLiteDatabase writableDatabase = this.dbHelper.getWritableDatabase();
        writableDatabase.delete(OffReadDbHelper.TABLE_HIGHLIGHTS, null, null);
        writableDatabase.delete(OffReadDbHelper.TABLE_READING_LOGS, null, null);
        writableDatabase.delete(OffReadDbHelper.TABLE_ARTICLES, null, null);
    }

    public boolean toggleFavorite(long j) {
        Article article = getArticle(j);
        if (article == null) {
            return false;
        }
        article.setFavorite(!article.isFavorite());
        updateArticle(article);
        return article.isFavorite();
    }

    public boolean setFavorite(long j, boolean z) {
        SQLiteDatabase writableDatabase = this.dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(OffReadDbHelper.COL_IS_FAVORITE, Integer.valueOf(z ? 1 : 0));
        contentValues.put(OffReadDbHelper.COL_UPDATED_AT, Long.valueOf(System.currentTimeMillis()));
        return writableDatabase.update(OffReadDbHelper.TABLE_ARTICLES, contentValues, "_id = ?", new String[]{String.valueOf(j)}) > 0;
    }

    public boolean toggleArchive(long j) {
        Article article = getArticle(j);
        if (article == null) {
            return false;
        }
        article.setArchived(!article.isArchived());
        updateArticle(article);
        return article.isArchived();
    }

    public void updateReadingProgress(long j, int i, int i2) {
        SQLiteDatabase writableDatabase = this.dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(OffReadDbHelper.COL_SCROLL_POS, Integer.valueOf(i));
        contentValues.put(OffReadDbHelper.COL_PROGRESS, Integer.valueOf(Math.max(0, Math.min(100, i2))));
        contentValues.put(OffReadDbHelper.COL_LAST_READ_AT, Long.valueOf(System.currentTimeMillis()));
        if (i2 >= 95) {
            contentValues.put(OffReadDbHelper.COL_IS_ARCHIVED, (Integer) 1);
        }
        writableDatabase.update(OffReadDbHelper.TABLE_ARTICLES, contentValues, "_id = ?", new String[]{String.valueOf(j)});
    }

    public void markAsCompleted(long j) {
        SQLiteDatabase writableDatabase = this.dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(OffReadDbHelper.COL_PROGRESS, (Integer) 100);
        contentValues.put(OffReadDbHelper.COL_IS_ARCHIVED, (Integer) 1);
        contentValues.put(OffReadDbHelper.COL_LAST_READ_AT, Long.valueOf(System.currentTimeMillis()));
        writableDatabase.update(OffReadDbHelper.TABLE_ARTICLES, contentValues, "_id = ?", new String[]{String.valueOf(j)});
    }

    public List<Article> getArticles(FilterType filterType, String str, String str2, SortOrder sortOrder) {
        String string;
        String str3;
        SQLiteDatabase readableDatabase = this.dbHelper.getReadableDatabase();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        if (filterType == FilterType.UNREAD) {
            arrayList.add("is_archived = 0");
        } else if (filterType == FilterType.FAVORITES) {
            arrayList.add("is_favorite = 1");
        } else if (filterType == FilterType.ARCHIVE) {
            arrayList.add("is_archived = 1");
        }
        if (str != null && !str.trim().isEmpty()) {
            String str4 = "%" + str.trim() + "%";
            arrayList.add("(title LIKE ? OR plain_text LIKE ? OR author LIKE ? OR domain LIKE ? OR tags LIKE ?)");
            arrayList2.add(str4);
            arrayList2.add(str4);
            arrayList2.add(str4);
            arrayList2.add(str4);
            arrayList2.add(str4);
        }
        if (str2 != null && !str2.trim().isEmpty()) {
            arrayList.add("tags LIKE ?");
            arrayList2.add("%" + str2.trim() + "%");
        }
        if (arrayList.isEmpty()) {
            string = null;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arrayList.size(); i++) {
                if (i > 0) {
                    sb.append(" AND ");
                }
                sb.append((String) arrayList.get(i));
            }
            string = sb.toString();
        }
        String str5 = string;
        if (sortOrder == SortOrder.OLDEST) {
            str3 = "created_at ASC";
        } else if (sortOrder == SortOrder.READING_TIME) {
            str3 = "reading_time_minutes ASC";
        } else if (sortOrder != SortOrder.TITLE) {
            str3 = "created_at DESC";
        } else {
            str3 = "title COLLATE NOCASE ASC";
        }
        Cursor cursorQuery = readableDatabase.query(OffReadDbHelper.TABLE_ARTICLES, null, str5, (String[]) arrayList2.toArray(new String[0]), null, null, str3);
        ArrayList arrayList3 = new ArrayList();
        if (cursorQuery == null || !cursorQuery.moveToFirst()) {
            if (cursorQuery != null) {
                cursorQuery.close();
            }
            return arrayList3;
        }
        do {
            arrayList3.add(mapCursorToArticle(cursorQuery));
        } while (cursorQuery.moveToNext());
        cursorQuery.close();
        return arrayList3;
    }

    public List<Article> getBookmarks() {
        return getArticles(FilterType.FAVORITES, null, null, SortOrder.NEWEST);
    }

    public List<Article> getAllArticlesForExport() {
        return getArticles(FilterType.ALL, null, null, SortOrder.NEWEST);
    }

    public Set<String> getAllTags() {
        List<Article> allArticlesForExport = getAllArticlesForExport();
        HashSet hashSet = new HashSet();
        Iterator<Article> it = allArticlesForExport.iterator();
        while (it.hasNext()) {
            String tags = it.next().getTags();
            if (tags != null && !tags.trim().isEmpty()) {
                for (String str : tags.split(",")) {
                    String strTrim = str.trim();
                    if (!strTrim.isEmpty()) {
                        hashSet.add(strTrim);
                    }
                }
            }
        }
        return hashSet;
    }

    public void recordReadingSession(long j, int i, int i2) {
        if (i > 0 || i2 > 0) {
            SQLiteDatabase writableDatabase = this.dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("article_id", Long.valueOf(j));
            contentValues.put(OffReadDbHelper.COL_LOG_SECONDS, Integer.valueOf(i));
            contentValues.put(OffReadDbHelper.COL_LOG_WORDS, Integer.valueOf(i2));
            contentValues.put("created_at", Long.valueOf(System.currentTimeMillis()));
            writableDatabase.insert(OffReadDbHelper.TABLE_READING_LOGS, null, contentValues);
        }
    }

    public ReadingStats getReadingStats() {
        int i;
        int i2;
        int i3;
        int i4;
        SQLiteDatabase readableDatabase = this.dbHelper.getReadableDatabase();
        Cursor cursorRawQuery = readableDatabase.rawQuery("SELECT COUNT(*), SUM(CASE WHEN progress_percent >= 95 OR is_archived = 1 THEN 1 ELSE 0 END) FROM articles", null);
        int i5 = 0;
        if (cursorRawQuery == null || !cursorRawQuery.moveToFirst()) {
            if (cursorRawQuery != null) {
                cursorRawQuery.close();
            }
            i = 0;
            i2 = 0;
        } else {
            int i6 = cursorRawQuery.getInt(0);
            int i7 = cursorRawQuery.getInt(1);
            cursorRawQuery.close();
            i = i6;
            i2 = i7;
        }
        Cursor cursorRawQuery2 = readableDatabase.rawQuery("SELECT SUM(seconds_read), SUM(words_read) FROM reading_logs", null);
        if (cursorRawQuery2 == null || !cursorRawQuery2.moveToFirst()) {
            if (cursorRawQuery2 != null) {
                cursorRawQuery2.close();
            }
            i3 = 0;
            i4 = 0;
        } else {
            int i8 = cursorRawQuery2.getInt(0);
            int i9 = cursorRawQuery2.getInt(1);
            cursorRawQuery2.close();
            i4 = i8;
            i3 = i9;
        }
        Cursor cursorRawQuery3 = readableDatabase.rawQuery("SELECT DISTINCT strftime('%Y-%m-%d', datetime(created_at/1000, 'unixepoch', 'localtime')) AS day FROM reading_logs ORDER BY day DESC LIMIT 30", null);
        if (cursorRawQuery3 != null) {
            int count = cursorRawQuery3.getCount();
            i5 = (count != 0 || i <= 0) ? count : 1;
            cursorRawQuery3.close();
        }
        return new ReadingStats(i, i2, i3, i4, Math.max(1, i5));
    }

    public long addHighlight(Highlight highlight) {
        SQLiteDatabase writableDatabase = this.dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("article_id", Long.valueOf(highlight.getArticleId()));
        contentValues.put(OffReadDbHelper.COL_HL_TEXT, highlight.getText());
        contentValues.put(OffReadDbHelper.COL_HL_NOTE, highlight.getNote());
        contentValues.put(OffReadDbHelper.COL_HL_COLOR, highlight.getColor());
        contentValues.put("created_at", Long.valueOf(highlight.getCreatedAt()));
        long jInsert = writableDatabase.insert(OffReadDbHelper.TABLE_HIGHLIGHTS, null, contentValues);
        highlight.setId(jInsert);
        return jInsert;
    }

    public List<Highlight> getHighlightsForArticle(long j) {
        Cursor cursorQuery = this.dbHelper.getReadableDatabase().query(OffReadDbHelper.TABLE_HIGHLIGHTS, null, "article_id = ?", new String[]{String.valueOf(j)}, null, null, "created_at ASC");
        ArrayList arrayList = new ArrayList();
        if (cursorQuery == null || !cursorQuery.moveToFirst()) {
            if (cursorQuery != null) {
                cursorQuery.close();
            }
            return arrayList;
        }
        do {
            arrayList.add(new Highlight(cursorQuery.getLong(cursorQuery.getColumnIndexOrThrow("_id")), cursorQuery.getLong(cursorQuery.getColumnIndexOrThrow("article_id")), cursorQuery.getString(cursorQuery.getColumnIndexOrThrow(OffReadDbHelper.COL_HL_TEXT)), cursorQuery.getString(cursorQuery.getColumnIndexOrThrow(OffReadDbHelper.COL_HL_NOTE)), cursorQuery.getString(cursorQuery.getColumnIndexOrThrow(OffReadDbHelper.COL_HL_COLOR)), cursorQuery.getLong(cursorQuery.getColumnIndexOrThrow("created_at"))));
        } while (cursorQuery.moveToNext());
        cursorQuery.close();
        return arrayList;
    }

    private Article mapCursorToArticle(Cursor cursor) {
        Article article = new Article();
        article.setId(cursor.getLong(cursor.getColumnIndexOrThrow("_id")));
        article.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_TITLE)));
        article.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_AUTHOR)));
        article.setUrl(cursor.getString(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_URL)));
        article.setDomain(cursor.getString(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_DOMAIN)));
        article.setContent(cursor.getString(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_CONTENT)));
        article.setPlainText(cursor.getString(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_PLAIN_TEXT)));
        article.setExcerpt(cursor.getString(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_EXCERPT)));
        article.setWordCount(cursor.getInt(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_WORD_COUNT)));
        article.setReadingTimeMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_READING_TIME)));
        article.setScrollPosition(cursor.getInt(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_SCROLL_POS)));
        article.setProgressPercent(cursor.getInt(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_PROGRESS)));
        article.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_IS_FAVORITE)) == 1);
        article.setArchived(cursor.getInt(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_IS_ARCHIVED)) == 1);
        article.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_CATEGORY)));
        article.setTags(cursor.getString(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_TAGS)));
        article.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        article.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_UPDATED_AT)));
        article.setLastReadAt(cursor.getLong(cursor.getColumnIndexOrThrow(OffReadDbHelper.COL_LAST_READ_AT)));
        int columnIndex = cursor.getColumnIndex(OffReadDbHelper.COL_CANONICAL_URL);
        if (columnIndex != -1) {
            article.setCanonicalUrl(cursor.getString(columnIndex));
        }
        int columnIndex2 = cursor.getColumnIndex(OffReadDbHelper.COL_PUBLISHED_DATE);
        if (columnIndex2 != -1) {
            article.setPublishedDate(cursor.getString(columnIndex2));
        }
        int columnIndex3 = cursor.getColumnIndex(OffReadDbHelper.COL_IMAGE_URL);
        if (columnIndex3 != -1) {
            article.setImageUrl(cursor.getString(columnIndex3));
        }
        int columnIndex4 = cursor.getColumnIndex(OffReadDbHelper.COL_LANGUAGE);
        if (columnIndex4 != -1) {
            article.setLanguage(cursor.getString(columnIndex4));
        }
        return article;
    }

    public Article findArticleByUrl(String str) {
        if (str != null && !str.trim().isEmpty()) {
            Cursor cursorQuery = this.dbHelper.getReadableDatabase().query(OffReadDbHelper.TABLE_ARTICLES, null, "url = ?", new String[]{str.trim()}, null, null, null, "1");
            if (cursorQuery != null && cursorQuery.moveToFirst()) {
                Article articleMapCursorToArticle = mapCursorToArticle(cursorQuery);
                cursorQuery.close();
                return articleMapCursorToArticle;
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        }
        return null;
    }
}
