package com.example.avi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.Nullable;

import com.example.avi.Journals.Journal;
import com.example.avi.Snapshot.Snapshot;

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

        String create_journals = "Create Table journals (journalID Integer IDENTITY(1,1) PRIMARY KEY, name VARCHAR(100) UNIQUE, " +
                "description VARCHAR(200), is_tracking Integer)";
        db.execSQL(create_journals);

        String data_points= "Create Table data_points (DataID Integer PRIMARY KEY, journal_name VARCHAR(100), " +
                "latitude Double(40, 20), longitude Double(40,20), FOREIGN KEY (journal_name) REFERENCES journals(name) ON DELETE CASCADE)";
        db.execSQL(data_points);

        String danger = "Create Table danger (location Integer, danger Integer, image_url VARCHAR(100), " +
                "overall_danger VARCHAR(20), region VARCHAR(20), date VARCHAR(20))";

        db.execSQL(danger);


        String snapshot = "Create Table snapshot (name VARCHAR(100), elevation VARCHAR(20), aspect VARCHAR(10), " +
                "danger VARCHAR(50), date VARCHAR(50))";

        db.execSQL(snapshot);
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

    public void addToJournals(String name, String description, int is_tracking){
        ContentValues vals = new ContentValues();
        vals.put("name", name);
        vals.put("description", description);
        vals.put("is_tracking", is_tracking);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insertOrThrow("journals", null, vals);
        db.close();
    }

    public void addToDanger(int loc, int danger, String image, String overall, String region, String date){
        ContentValues vals = new ContentValues();
        vals.put("location", loc);
        vals.put("danger", danger);
        vals.put("image_url", image);
        vals.put("overall_danger", overall);
        vals.put("region", region);
        vals.put("date", date);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insertOrThrow("danger", null, vals);
        db.close();
    }

    public void addToSnapshot(String name, String elevation, String aspect, String danger, String date){
        ContentValues vals = new ContentValues();
        vals.put("name", name);
        vals.put("elevation", elevation);
        vals.put("aspect", aspect);
        vals.put("danger", danger);
        vals.put("date", date);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insertOrThrow("snapshot", null, vals);
        db.close();
    }

    public void deleteFromJournals(String name){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("journals", "name = '" + name + "'", null);

        db.close();
    }

    public void deleteFromSnapshot(String date){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("snapshot", "date = '" + date + "'", null);

        db.close();
    }



    public void editJournal(String original_name, String name, String description, boolean is_tracking)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("name",name); //These Fields should be your String values of actual column names
        cv.put("description",description);

        if(is_tracking)
        {
            cv.put("is_tracking", 1);
        }
        else
        {
            cv.put("is_tracking", 0);
        }

        db.update("journals", cv, "name= '"+original_name + "'", null);

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

                if(c.getInt(c.getColumnIndex("is_tracking")) == 1)
                {
                    j.start_recording = true;
                }
                else
                {
                    j.start_recording = false;
                }
                Journals.add(j);
                c.moveToNext();
            }
        }

        return Journals;
    }

    public ArrayList<Snapshot> getAllSnapshots(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select * from snapshot",null);

        ArrayList<Snapshot> snapshots = new ArrayList<Snapshot>();

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String name = c.getString(c.getColumnIndex("name"));
                String elevation = c.getString(c.getColumnIndex("elevation"));
                String aspect = c.getString(c.getColumnIndex("aspect"));
                String danger = c.getString(c.getColumnIndex("danger"));
                String date = c.getString(c.getColumnIndex("date"));
                Snapshot s = new Snapshot(name, elevation, aspect, danger, date);

                snapshots.add(s);
                c.moveToNext();

            }
        }

        return snapshots;
    }

    public void add_to_data_points(String name, Double lat, Double longitude)
    {
        ContentValues vals = new ContentValues();
        vals.put("journal_name", name);
        vals.put("latitude", lat);
        vals.put("longitude", longitude);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insertOrThrow("data_points", null, vals);
        db.close();
    }

    public ArrayList<Double> getAllData(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select * from data_points where journal_name='" + name + "'",null);
        //Cursor c = db.rawQuery("select * from data_points", null);
        ArrayList<Double> data_points = new ArrayList<Double>();

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                Double latitude = c.getDouble(c.getColumnIndex("latitude"));
                Double longitude = c.getDouble(c.getColumnIndex("longitude"));
                data_points.add(latitude);
                data_points.add(longitude);
                c.moveToNext();
            }
        }

        return data_points;
    }

    public String getDangerDate(){
        Cursor c = null;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            c = db.rawQuery("select date from danger limit 1", null);
            String date = "";
            if (c.moveToFirst()) {
                date = c.getString(c.getColumnIndex("date"));
            }
            c.close();
            return date;
        }
        finally {
            c.close();
        }
    }

    public int getDangerAtLocation(int loc){
        Cursor c = null;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            c = db.rawQuery("select danger from danger where location = " + loc, null);
            int danger = 0;
            if (c.moveToFirst()) {
                danger = c.getInt(c.getColumnIndex("danger"));
            }
            c.close();
            return danger;
        }
        finally {
            c.close();
        }
    }

    public void clearDangerTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "delete from danger";
        db.execSQL(query);
    }
}
