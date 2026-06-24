package com.example.kolokvijum2;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "kolokvijum2.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE = "continents";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "code TEXT, " +
                "name TEXT, " +
                "areaSqKm INTEGER, " +
                "population INTEGER, " +
                "countries INTEGER)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void insertContinent(Continent c) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("code", c.getCode());
        values.put("name", c.getName());
        values.put("areaSqKm", c.getAreaSqKm());
        values.put("population", c.getPopulation());
        values.put("countries", c.getCountries());

        db.insert(TABLE, null, values);
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, null, null);
    }

    public Integer getCountriesOfThirdContinent() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT countries FROM " + TABLE + " ORDER BY id ASC LIMIT 1 OFFSET 2",
                null
        );

        Integer countries = null;

        if (cursor.moveToFirst()) {
            countries = cursor.getInt(0);
        }

        cursor.close();
        return countries;
    }
}