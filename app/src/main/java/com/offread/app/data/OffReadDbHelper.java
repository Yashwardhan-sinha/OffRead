package com.offread.app.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class OffReadDbHelper extends SQLiteOpenHelper {
    public static final String COL_AUTHOR = "author";
    public static final String COL_CATEGORY = "category";
    public static final String COL_CONTENT = "content";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_DOMAIN = "domain";
    public static final String COL_EXCERPT = "excerpt";
    public static final String COL_HL_ARTICLE_ID = "article_id";
    public static final String COL_HL_COLOR = "color";
    public static final String COL_HL_CREATED_AT = "created_at";
    public static final String COL_HL_ID = "_id";
    public static final String COL_HL_NOTE = "note";
    public static final String COL_HL_TEXT = "highlight_text";
    public static final String COL_ID = "_id";
    public static final String COL_IS_ARCHIVED = "is_archived";
    public static final String COL_IS_FAVORITE = "is_favorite";
    public static final String COL_LAST_READ_AT = "last_read_at";
    public static final String COL_LOG_ARTICLE_ID = "article_id";
    public static final String COL_LOG_CREATED_AT = "created_at";
    public static final String COL_LOG_ID = "_id";
    public static final String COL_LOG_SECONDS = "seconds_read";
    public static final String COL_LOG_WORDS = "words_read";
    public static final String COL_PLAIN_TEXT = "plain_text";
    public static final String COL_PROGRESS = "progress_percent";
    public static final String COL_READING_TIME = "reading_time_minutes";
    public static final String COL_SCROLL_POS = "scroll_position";
    public static final String COL_TAGS = "tags";
    public static final String COL_TITLE = "title";
    public static final String COL_UPDATED_AT = "updated_at";
    public static final String COL_URL = "url";
    public static final String COL_WORD_COUNT = "word_count";
    public static final String DATABASE_NAME = "offread.db";
    public static final int DATABASE_VERSION = 3;
    private static final String SQL_CREATE_ARTICLES = "CREATE TABLE articles (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, author TEXT, url TEXT, domain TEXT, content TEXT, plain_text TEXT, excerpt TEXT, word_count INTEGER DEFAULT 0, reading_time_minutes INTEGER DEFAULT 1, scroll_position INTEGER DEFAULT 0, progress_percent INTEGER DEFAULT 0, is_favorite INTEGER DEFAULT 0, is_archived INTEGER DEFAULT 0, category TEXT DEFAULT 'General', tags TEXT, created_at INTEGER, updated_at INTEGER, last_read_at INTEGER, canonical_url TEXT, published_date TEXT, image_url TEXT, article_language TEXT);";
    private static final String SQL_CREATE_HIGHLIGHTS = "CREATE TABLE highlights (_id INTEGER PRIMARY KEY AUTOINCREMENT, article_id INTEGER NOT NULL, highlight_text TEXT NOT NULL, note TEXT, color TEXT DEFAULT '#F59E0B', created_at INTEGER, FOREIGN KEY(article_id) REFERENCES articles(_id) ON DELETE CASCADE);";
    private static final String SQL_CREATE_READING_LOGS = "CREATE TABLE reading_logs (_id INTEGER PRIMARY KEY AUTOINCREMENT, article_id INTEGER, seconds_read INTEGER DEFAULT 0, words_read INTEGER DEFAULT 0, created_at INTEGER);";
    public static final String TABLE_ARTICLES = "articles";
    public static final String TABLE_HIGHLIGHTS = "highlights";
    public static final String TABLE_READING_LOGS = "reading_logs";
    public static final String COL_CANONICAL_URL = "canonical_url";
    public static final String COL_PUBLISHED_DATE = "published_date";
    public static final String COL_IMAGE_URL = "image_url";
    public static final String COL_LANGUAGE = "article_language";
    private static final String[] REQUIRED_ARTICLE_COLUMNS = {COL_CANONICAL_URL, COL_PUBLISHED_DATE, COL_IMAGE_URL, COL_LANGUAGE};

    public OffReadDbHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 3);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL(SQL_CREATE_ARTICLES);
        sQLiteDatabase.execSQL(SQL_CREATE_HIGHLIGHTS);
        sQLiteDatabase.execSQL(SQL_CREATE_READING_LOGS);
    }

    public static String[] requiredArticleColumns() {
        return (String[]) REQUIRED_ARTICLE_COLUMNS.clone();
    }

    public static String articlesCreateSql() {
        return SQL_CREATE_ARTICLES;
    }

    public static String[] addedColumnsForUpgrade(int i) {
        ArrayList arrayList = new ArrayList();
        if (i < 2) {
            arrayList.add(COL_CANONICAL_URL);
            arrayList.add(COL_PUBLISHED_DATE);
            arrayList.add(COL_IMAGE_URL);
        }
        if (i < 3) {
            arrayList.add(COL_LANGUAGE);
        }
        return (String[]) arrayList.toArray(new String[0]);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        for (String str : addedColumnsForUpgrade(i)) {
            addColumnIfMissing(sQLiteDatabase, str);
        }
        for (String str2 : REQUIRED_ARTICLE_COLUMNS) {
            if (!hasColumn(sQLiteDatabase, str2)) {
                throw new IllegalStateException("OffRead database upgrade did not add " + str2 + " to table articles");
            }
        }
    }

    private void addColumnIfMissing(SQLiteDatabase sQLiteDatabase, String str) {
        if (hasColumn(sQLiteDatabase, str)) {
            return;
        }
        sQLiteDatabase.execSQL("ALTER TABLE articles ADD COLUMN " + str + " TEXT;");
    }

    private boolean hasColumn(SQLiteDatabase sQLiteDatabase, String str) {
        Cursor cursorRawQuery = sQLiteDatabase.rawQuery("PRAGMA table_info(articles)", null);
        if (cursorRawQuery != null) {
            try {
                if (cursorRawQuery.moveToFirst()) {
                    int columnIndex = cursorRawQuery.getColumnIndex("name");
                    if (columnIndex < 0) {
                        return false;
                    }
                    while (!str.equalsIgnoreCase(cursorRawQuery.getString(columnIndex))) {
                        if (!cursorRawQuery.moveToNext()) {
                            return false;
                        }
                    }
                    return true;
                }
            } finally {
                if (cursorRawQuery != null) {
                    cursorRawQuery.close();
                }
            }
        }
        return false;
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onConfigure(SQLiteDatabase sQLiteDatabase) {
        super.onConfigure(sQLiteDatabase);
        sQLiteDatabase.setForeignKeyConstraintsEnabled(true);
    }
}
