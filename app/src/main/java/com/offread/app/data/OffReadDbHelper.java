package com.offread.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OffReadDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "offread.db";
    public static final int DATABASE_VERSION = 1;

    // Table: articles
    public static final String TABLE_ARTICLES = "articles";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_AUTHOR = "author";
    public static final String COL_URL = "url";
    public static final String COL_DOMAIN = "domain";
    public static final String COL_CONTENT = "content";
    public static final String COL_PLAIN_TEXT = "plain_text";
    public static final String COL_EXCERPT = "excerpt";
    public static final String COL_WORD_COUNT = "word_count";
    public static final String COL_READING_TIME = "reading_time_minutes";
    public static final String COL_SCROLL_POS = "scroll_position";
    public static final String COL_PROGRESS = "progress_percent";
    public static final String COL_IS_FAVORITE = "is_favorite";
    public static final String COL_IS_ARCHIVED = "is_archived";
    public static final String COL_CATEGORY = "category";
    public static final String COL_TAGS = "tags";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";
    public static final String COL_LAST_READ_AT = "last_read_at";

    // Table: highlights
    public static final String TABLE_HIGHLIGHTS = "highlights";
    public static final String COL_HL_ID = "_id";
    public static final String COL_HL_ARTICLE_ID = "article_id";
    public static final String COL_HL_TEXT = "highlight_text";
    public static final String COL_HL_NOTE = "note";
    public static final String COL_HL_COLOR = "color";
    public static final String COL_HL_CREATED_AT = "created_at";

    // Table: reading_logs
    public static final String TABLE_READING_LOGS = "reading_logs";
    public static final String COL_LOG_ID = "_id";
    public static final String COL_LOG_ARTICLE_ID = "article_id";
    public static final String COL_LOG_SECONDS = "seconds_read";
    public static final String COL_LOG_WORDS = "words_read";
    public static final String COL_LOG_CREATED_AT = "created_at";

    private static final String SQL_CREATE_ARTICLES =
            "CREATE TABLE " + TABLE_ARTICLES + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TITLE + " TEXT NOT NULL, " +
                    COL_AUTHOR + " TEXT, " +
                    COL_URL + " TEXT, " +
                    COL_DOMAIN + " TEXT, " +
                    COL_CONTENT + " TEXT, " +
                    COL_PLAIN_TEXT + " TEXT, " +
                    COL_EXCERPT + " TEXT, " +
                    COL_WORD_COUNT + " INTEGER DEFAULT 0, " +
                    COL_READING_TIME + " INTEGER DEFAULT 1, " +
                    COL_SCROLL_POS + " INTEGER DEFAULT 0, " +
                    COL_PROGRESS + " INTEGER DEFAULT 0, " +
                    COL_IS_FAVORITE + " INTEGER DEFAULT 0, " +
                    COL_IS_ARCHIVED + " INTEGER DEFAULT 0, " +
                    COL_CATEGORY + " TEXT DEFAULT 'General', " +
                    COL_TAGS + " TEXT, " +
                    COL_CREATED_AT + " INTEGER, " +
                    COL_UPDATED_AT + " INTEGER, " +
                    COL_LAST_READ_AT + " INTEGER" +
                    ");";

    private static final String SQL_CREATE_HIGHLIGHTS =
            "CREATE TABLE " + TABLE_HIGHLIGHTS + " (" +
                    COL_HL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_HL_ARTICLE_ID + " INTEGER NOT NULL, " +
                    COL_HL_TEXT + " TEXT NOT NULL, " +
                    COL_HL_NOTE + " TEXT, " +
                    COL_HL_COLOR + " TEXT DEFAULT '#F59E0B', " +
                    COL_HL_CREATED_AT + " INTEGER, " +
                    "FOREIGN KEY(" + COL_HL_ARTICLE_ID + ") REFERENCES " + TABLE_ARTICLES + "(" + COL_ID + ") ON DELETE CASCADE" +
                    ");";

    private static final String SQL_CREATE_READING_LOGS =
            "CREATE TABLE " + TABLE_READING_LOGS + " (" +
                    COL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_LOG_ARTICLE_ID + " INTEGER, " +
                    COL_LOG_SECONDS + " INTEGER DEFAULT 0, " +
                    COL_LOG_WORDS + " INTEGER DEFAULT 0, " +
                    COL_LOG_CREATED_AT + " INTEGER" +
                    ");";

    public OffReadDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ARTICLES);
        db.execSQL(SQL_CREATE_HIGHLIGHTS);
        db.execSQL(SQL_CREATE_READING_LOGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIGHLIGHTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_READING_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTICLES);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
