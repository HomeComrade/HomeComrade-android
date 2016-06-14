package com.shiznatix.homecomrade;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableLastDirectory extends TableAbstract {
    static private final String LOG_TAG = "mc_LastDirectoryTable";

    protected static final String TABLE_NAME = "lastDirectory";

    protected static final String KEY_ID = "lastDirectoryid";
    protected static final String KEY_URL = "url";
    protected static final String KEY_DIRECTORY = "directory";

    public TableLastDirectory(Context context) {
        super(context);
    }

    public void saveLastDirectory(EntityLastDirectory entityLastDirectory) {
        ContentValues values = new ContentValues();
        values.put(KEY_URL, entityLastDirectory.url);
        values.put(KEY_DIRECTORY, entityLastDirectory.directory);

        EntityLastDirectory checkEntityLastDirectory = getLastDirectoryByServer(entityLastDirectory.url);

        SQLiteDatabase db = getWritableDatabase();

        if (null == checkEntityLastDirectory) {
            Log.i(LOG_TAG, "inserting new directory: "+ entityLastDirectory.url);
            db.insert(TABLE_NAME, null, values);
        }
        else {
            Log.i(LOG_TAG, "updating directory: "+ entityLastDirectory.url);
            db.update(TABLE_NAME, values, null, null);
        }

        db.close();
    }

    public EntityLastDirectory getLastDirectoryByServer(String url) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_URL, KEY_DIRECTORY }, KEY_URL+" = ?", new String[] { url }, null, null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            EntityLastDirectory entityLastDirectory = new EntityLastDirectory(cursor.getInt(0), cursor.getString(1), cursor.getString(2));

            db.close();
            cursor.close();

            return entityLastDirectory;
        }

        db.close();
        cursor.close();

        return null;
    }
}