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
        String query = "Create Table users (userID Integer PRIMARY KEY, userRegistration VARCHAR(100) UNIQUE, " +
                "userPassword VARCHAR(50), userFirstName VARCHAR(50), userLastName VARCHAR(50), " +
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

    public void addToUsers(String userRegistration, String firstName, String lastName, String password, boolean isEmail){
        ContentValues vals = new ContentValues();
        vals.put("userRegistration", userRegistration);
        vals.put("userPassword", password);
        vals.put("userFirstName", firstName);
        vals.put("userLastName", lastName);
        if(isEmail){
            vals.put("userRegistrationMethod", "Email");
        }
        else{
            vals.put("userRegistrationMethod", "Facebook");
        }
        SQLiteDatabase db = this.getWritableDatabase();
        db.insertOrThrow("users", null, vals);
        db.close();
    }

    public String findUser(String reg, String password){
        String query = "Select userRegistration, userPassword from users " +
                "where userRegistration = \'" + reg + "\' and userPassword = \'" + password + "\'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
           cursor.moveToFirst();
           return cursor.getString(1);
        }
        else{
            return "";
        }

    }
}
