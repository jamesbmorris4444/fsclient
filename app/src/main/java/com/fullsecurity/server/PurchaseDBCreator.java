package com.fullsecurity.server;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class PurchaseDBCreator extends SQLiteOpenHelper {

    public static final String TABLE_PURCHASES = "purchases";
    public static final String ROWID = "rowid";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String STATUS = "status";
    public static final String COST = "cost";
    public static final String WEIGHT = "weight";

    private static final String DATABASE_NAME = "purchases.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table " + TABLE_PURCHASES + "(" +
            ROWID + " integer primary key autoincrement, " +
            NAME + " text, " +
            DESCRIPTION + " text, " +
            STATUS + " text, " +
            COST + " integer, " +
            WEIGHT + " integer" +
            ");";

    public PurchaseDBCreator(Context context) {
        super(context, Environment.getExternalStorageDirectory().toString() + "/Android/data/com.fullsecurity.shared/" + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("JIM", "Upgrading baseballDatabase from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASES);
        onCreate(db);
    }
}
