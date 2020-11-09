package com.example.avi.Journals;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class Journal implements Serializable {
    public String name;
    public ArrayList<LatLng> data_points;
    public String description;
    public boolean start_recording;
    public String type;
    public boolean show_line;

    public Journal() {

    }
}
