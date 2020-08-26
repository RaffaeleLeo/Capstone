package com.example.avi.Journals;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Journal {
    public String name;
    public ArrayList<LatLng> data_points;
    public String description;
    public boolean start_recording;

    public Journal(){

    }
}
