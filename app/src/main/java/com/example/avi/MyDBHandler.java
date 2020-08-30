package com.example.avi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.Nullable;

import com.example.avi.Journals.Journal;

import java.util.ArrayList;

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
        String query = "Create Table users (userID VARCHAR(100) PRIMARY KEY, userName VARCHAR(100), " +
                "userEmail VARCHAR(100), " +
                "userRegistrationMethod VARCHAR(8))";
        db.execSQL(query);

        String create_journals = "Create Table journals (journalID, Integer IDENTITY(1,1) PRIMARY KEY, name VARCHAR(100) UNIQUE, " +
                "description VARCHAR(200))";
        db.execSQL(create_journals);

        String data_points_table= "Create Table data_points (DataID Integer PRIMARY KEY, journal_name VARCHAR(100), " +
                "lat Integer, long Integer, FOREIGN KEY (journal_name) REFERENCES journals(name))";
        db.execSQL(data_points_table);
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


    public void addToJournals(String name, String description){
        ContentValues vals = new ContentValues();
        vals.put("name", name);
        vals.put("description", description);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insertOrThrow("journals", null, vals);
        db.close();
    }

    public void deleteFromJournals(String name){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("journals", "name = '" + name + "'", null);

        db.close();
    }

    public void editJournal(String name, String description)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("name",name); //These Fields should be your String values of actual column names
        cv.put("description",description);

        db.update("journals", cv, "name= '"+name + "'", null);

        db.close();
    }

    public ArrayList<Journal> getAllJournals(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select * from journals",null);

        ArrayList<Journal> Journals = new ArrayList<Journal>();

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                Journal j = new Journal();
                j.name = c.getString(c.getColumnIndex("name"));
                j.description = c.getString(c.getColumnIndex("description"));

                Journals.add(j);
                c.moveToNext();
            }
        }

        return Journals;
    }
}
