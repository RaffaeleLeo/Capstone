package com.example.avi;

import android.location.Location;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Tours {

    private String userId;
    //List of tourIds that a user has created or accepted
    private ArrayList<String> acceptedToursIds;
    //List of tourIds that a user has been invited to
    private ArrayList<String> pendingTourIds;

    public Tours(String userId, ArrayList<String> acceptedToursIds, ArrayList<String> pendingTourIds){
        this.userId = userId;
        this.acceptedToursIds = acceptedToursIds;
        this.pendingTourIds = pendingTourIds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArrayList<String> getAcceptedToursIds() {
        return acceptedToursIds;
    }

    public void setAcceptedToursIds(ArrayList<String> acceptedToursIds) {
        this.acceptedToursIds = acceptedToursIds;
    }

    public ArrayList<String> getPendingTourIds() {
        return pendingTourIds;
    }

    public void setPendingTourIds(ArrayList<String> pendingTourIds) {
        this.pendingTourIds = pendingTourIds;
    }

    static class Tour {
        //Tour Id, name, date, and owner are the only required for creation
        public String tourId;
        public String tourName;
        //One who created the tour and any with owner permissions
        public ArrayList<String> tourOwners;
        public String date;
        public String time;
        public String notes;
        public ArrayList<String> acceptedInvitees;
        public ArrayList<String> pendingInvitees;
        public ArrayList<String> declinedInvitees;
        public String latitude;
        public String longitude;

        public Tour(String tourId, String tourName, ArrayList<String> tourOwners, String date){
            this.tourId = tourId;
            this.tourName = tourName;
            this.tourOwners = tourOwners;
            this.date = date;
        }

        public String getTourId() {
            return tourId;
        }

        public void setTourId(String tourId) {
            this.tourId = tourId;
        }

        public String getTourName() {
            return tourName;
        }

        public void setTourName(String tourName) {
            this.tourName = tourName;
        }

        public ArrayList<String> getTourOwners() {
            return tourOwners;
        }

        public void setTourOwners(ArrayList<String> tourOwners) {
            this.tourOwners = tourOwners;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public ArrayList<String> getAcceptedInvitees() {
            return acceptedInvitees;
        }

        public void setAcceptedInvitees(ArrayList<String> acceptedInvitees) {
            this.acceptedInvitees = acceptedInvitees;
        }

        public ArrayList<String> getPendingInvitees() {
            return pendingInvitees;
        }

        public void setPendingInvitees(ArrayList<String> pendingInvitees) {
            this.pendingInvitees = pendingInvitees;
        }

        public ArrayList<String> getDeclinedInvitees() {
            return declinedInvitees;
        }

        public void setDeclinedInvitees(ArrayList<String> declinedInvitees) {
            this.declinedInvitees = declinedInvitees;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }
    }


}
