package com.homecomrade;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TableAbstract extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "mediaRemote";

    private static final String CREATE_SERVER_TABLE = "CREATE TABLE "+ TableServers.TABLE_NAME+"("
            + TableServers.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + TableServers.KEY_URL+" TEXT )";

    private static final String CREATE_RANDOM_SHOWS_TABLE = "CREATE TABLE "+ TableRandomShows.TABLE_NAME+"("
            + TableRandomShows.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + TableRandomShows.KEY_TITLE+" TEXT )";

    private static final String CREATE_DIRECTORIES_TABLE = "CREATE TABLE "+ TableLastDirectory.TABLE_NAME+"("
            + TableLastDirectory.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + TableLastDirectory.KEY_URL+" TEXT,"
            + TableLastDirectory.KEY_DIRECTORY+" TEXT )";

    public TableAbstract(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SERVER_TABLE);
        db.execSQL(CREATE_RANDOM_SHOWS_TABLE);
        db.execSQL(CREATE_DIRECTORIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}