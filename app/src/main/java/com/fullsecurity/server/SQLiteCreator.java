package com.fullsecurity.server;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteCreator extends SQLiteOpenHelper {

    public static final String TABLE_MASTER = "master";
    public static final String COLUMN_ID = "rowid";
    public static final String COLUMN_PLAYER_ID = "playerID";
    public static final String COLUMN_BIRTH_YEAR = "birthYear";
    public final static String COLUMN_BIRTH_MONTH = "birthMonth";
    public final static String COLUMN_BIRTHDAY = "birthDay";
    public final static String COLUMN_BIRTH_COUNTRY = "birthCountry";
    public final static String COLUMN_BIRTH_STATE = "birthState";
    public final static String COLUMN_BIRTH_CITY = "birthCity";
    public final static String COLUMN_DEATH_YEAR = "deathYear";
    public final static String COLUMN_DEATH_MONTH = "deathMonth";
    public final static String COLUMN_DEATH_DAY = "deathDay";
    public final static String COLUMN_DEATH_COUNTRY = "deathCountry";
    public final static String COLUMN_DEATH_STATE = "deathState";
    public final static String COLUMN_DEATH_CITY = "deathCity";
    public final static String COLUMN_NAME_FIRST = "nameFirst";
    public final static String COLUMN_NAME_LAST = "nameLast";
    public final static String COLUMN_NAME_GIVEN = "nameGiven";
    public final static String COLUMN_WEIGHT = "weight";
    public final static String COLUMN_HEIGHT = "height";
    public final static String COLUMN_BATS = "bats";
    public final static String COLUMN_THROWS = "throws";
    public final static String COLUMN_DEBUT = "debut";
    public final static String COLUMN_FINAL_GAME = "finalGame";
    public final static String COLUMN_RETRO_ID = "retroID";
    public final static String COLUMN_BBREF_ID = "bbrefID";

    private static final String DATABASE_NAME = "baseball.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table " + TABLE_MASTER + "(" +
        COLUMN_ID + " integer primary key autoincrement, " +
        COLUMN_PLAYER_ID + " text, " +
        COLUMN_BIRTH_YEAR + " integer, " +
        COLUMN_BIRTH_MONTH + " integer, " +
        COLUMN_BIRTHDAY + " integer, " +
        COLUMN_BIRTH_COUNTRY + " text, " +
        COLUMN_BIRTH_STATE + " text, " +
        COLUMN_BIRTH_CITY + " text, " +
        COLUMN_DEATH_YEAR + " integer, " +
        COLUMN_DEATH_MONTH + " integer, " +
        COLUMN_DEATH_DAY + " integer, " +
        COLUMN_DEATH_COUNTRY + " text, " +
        COLUMN_DEATH_STATE + " text, " +
        COLUMN_DEATH_CITY + " text, " +
        COLUMN_NAME_FIRST + " text, " +
        COLUMN_NAME_LAST + " text, " +
        COLUMN_NAME_GIVEN + " text, " +
        COLUMN_WEIGHT + " integer, " +
        COLUMN_HEIGHT + " integer, " +
        COLUMN_BATS + " text, " +
        COLUMN_THROWS + " text, " +
        COLUMN_DEBUT + " text, " +
        COLUMN_FINAL_GAME + " text, " +
        COLUMN_RETRO_ID + " text, " +
        COLUMN_BBREF_ID + " text " +
    ");";

    public SQLiteCreator(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("JIM","Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MASTER);
        onCreate(db);
    }

}
