package com.homecomrade;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class TableServers extends TableAbstract {
    static private final String LOG_TAG = "mc_ServersTable";

    protected static final String TABLE_NAME = "servers";

    protected static final String KEY_ID = "serverid";
    protected static final String KEY_URL = "url";

    public TableServers(Context context) {
        super(context);
    }

    public void addServer(String url) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_URL, url);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public EntityServer getServer(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_URL }, KEY_ID+" = ?", new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            EntityServer entityServer = new EntityServer(cursor.getInt(0), cursor.getString(1));

            db.close();
            cursor.close();

            return entityServer;
        }

        db.close();
        cursor.close();

        return null;
    }

    public EntityServer getServerByUrl(String url) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_URL }, KEY_URL+" = ?", new String[] { url }, null, null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            EntityServer entityServer = new EntityServer(cursor.getInt(0), cursor.getString(1));

            db.close();
            cursor.close();

            return entityServer;
        }

        db.close();
        cursor.close();

        return null;
    }

    public ArrayList<EntityServer> getAllServers() {
        ArrayList<EntityServer> entityServerList = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+TABLE_NAME;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                EntityServer contact = new EntityServer(cursor.getInt(0), cursor.getString(1));

                entityServerList.add(contact);
            }
            while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        return entityServerList;
    }

    public void deleteServer(EntityServer entityServer) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID+" = ?", new String[] { String.valueOf(entityServer.serverid) });
        db.close();

        Log.i(LOG_TAG, "server deleted");
    }
}