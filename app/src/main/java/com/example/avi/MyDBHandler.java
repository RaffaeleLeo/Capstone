package com.example.avi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.Nullable;

public class MyDBHandler extends SQLiteOpenHelper {

    private final static int version = 1;
    private final static String dbName = "database.db";

    public MyDBHandler(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //String query = "Create Table userCoordinates (userID Integer, latX Double, latY Double)";
        //db.execSQL(query);
        String query = "drop table users";
        db.execSQL(query);
        query = "Create Table users (userID VARCHAR(100) PRIMARY KEY, userName VARCHAR(100), " +
                "userEmail VARCHAR(100), " +
                "userRegistrationMethod VARCHAR(8))";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

/*    public void addToUserCoordinates(int userID, double latX, double latY){
        ContentValues vals = new ContentValues();
        vals.put("userID", userID);
        vals.put("latX", latX);
        vals.put("latY", latY);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("userCoordinates", null, vals);
        db.close();
    }*/

    public void addToUsers(String uID, String name, String email, String regMethod){
        ContentValues vals = new ContentValues();
        vals.put("userID", uID);
        vals.put("userName", name);
        vals.put("userEmail", email);
        vals.put("userRegistrationMethod", regMethod);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insertOrThrow("users", null, vals);
        db.close();
    }


}
