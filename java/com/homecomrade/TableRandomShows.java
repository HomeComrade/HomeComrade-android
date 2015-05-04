package com.homecomrade;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class TableRandomShows extends TableAbstract {
    static private final String LOG_TAG = "mc_RandomSettingsTable";

    protected static final String TABLE_NAME = "randomShows";

    protected static final String KEY_ID = "randomShowid";
    protected static final String KEY_TITLE = "title";

    public TableRandomShows(Context context) {
        super(context);
    }

    public void addShow(String title) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public EntityRandomShow getShow(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_TITLE }, KEY_ID+" = ?", new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            EntityRandomShow entityRandomShow = new EntityRandomShow(cursor.getInt(0), cursor.getString(1));

            db.close();
            cursor.close();

            return entityRandomShow;
        }

        db.close();
        cursor.close();

        return null;
    }

    public EntityRandomShow getShowByTitle(String title) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_TITLE }, KEY_TITLE+" = ?", new String[] { title }, null, null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            EntityRandomShow entityRandomShow = new EntityRandomShow(cursor.getInt(0), cursor.getString(1));

            db.close();
            cursor.close();

            return entityRandomShow;
        }

        db.close();
        cursor.close();

        return null;
    }

    public ArrayList<EntityRandomShow> getAllShows() {
        ArrayList<EntityRandomShow> entityRandomShowList = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+TABLE_NAME;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                EntityRandomShow entityRandomShow = new EntityRandomShow(cursor.getInt(0), cursor.getString(1));

                entityRandomShowList.add(entityRandomShow);
            }
            while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        return entityRandomShowList;
    }

    public ArrayList<String> getAllShowTitles() {
        ArrayList<String> randomShowTitles = new ArrayList<>();

        String selectQuery = "SELECT "+KEY_TITLE+" FROM "+TABLE_NAME;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                randomShowTitles.add(cursor.getString(0));
            }
            while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        return randomShowTitles;
    }

    public void deleteShow(EntityRandomShow entityRandomShow) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID+" = ?", new String[] { String.valueOf(entityRandomShow.randomShowid) });
        db.close();

        Log.i(LOG_TAG, "show deleted");
    }
}