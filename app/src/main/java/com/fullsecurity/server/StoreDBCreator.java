package com.fullsecurity.server;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class StoreDBCreator extends SQLiteOpenHelper {

    public static final String TABLE_PRODUCTS = "products";
    public static final String ROWID = "rowid";
    public static final String CATEGORY = "category";
    public static final String DESCRIPTION = "description";
    public final static String COST = "cost";
    public final static String WEIGHT = "weight";

    private static final String DATABASE_NAME = "products.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table " + TABLE_PRODUCTS + "(" +
            ROWID + " integer primary key autoincrement, " +
            CATEGORY + " text, " +
            DESCRIPTION + " text, " +
            COST + " integer, " +
            WEIGHT + " integer" +
            ");";

    public StoreDBCreator(Context context) {
        super(context, Environment.getExternalStorageDirectory().toString() + "/Android/data/com.fullsecurity.shared/" + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("JIM", "Upgrading baseballDatabase from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }
}
