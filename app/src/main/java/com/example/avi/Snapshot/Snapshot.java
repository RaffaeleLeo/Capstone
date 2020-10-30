package com.example.avi.Snapshot;

public class Snapshot {
    private String name;
    private String elevation;
    private String aspect;
    private String rating;
    private String date;

    public Snapshot(String name, String elevation, String aspect, String rating, String date) {
        this.name = name;
        this.elevation = elevation;
        this.aspect = aspect;
        this.rating = rating;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getElevation() {
        return elevation;
    }

    public String getAspect() {
        return aspect;
    }

    public String getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }

}
