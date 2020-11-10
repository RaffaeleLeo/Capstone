package com.example.avi;

import java.util.ArrayList;
import java.util.List;

public class Tours{

    //List of tourIds that a user has created or accepted
    public List<String> acceptedTourIds;
    //List of tourIds that a user has been invited to
    public List<String> pendingTourIds;

    public Tours() {
    }

    public Tours(List<String> acceptedTourIds, List<String> pendingTourIds){
        this.acceptedTourIds = acceptedTourIds;
        this.pendingTourIds = pendingTourIds;
    }

    public List<String> getAcceptedTourIds() {
        return acceptedTourIds;
    }

    public void setAcceptedTourIds(ArrayList<String> acceptedTourIds) {
        this.acceptedTourIds = acceptedTourIds;
    }

    public List<String> getPendingTourIds() {
        return pendingTourIds;
    }

    public void setPendingTourIds(ArrayList<String> pendingTourIds) {
        this.pendingTourIds = pendingTourIds;
    }

    public void sortTours(){

    }


    static class Tour //implements Comparable<Tour>
    {
        //Tour name, date, and owner are the only required for creation
        public String tourName;
        //One who created the tour and any with owner permissions
        public List<String> tourOwners;
        public String date;
        public String time;
        public String notes;
        public List<String> acceptedInvitees;
        public List<String> pendingInvitees;
        public String latLon;

        public Tour() {
        }

        public Tour(String tourName, List<String> tourOwners, String date, String time, String notes, List<String> acceptedInvitees, List<String> pendingInvitees, String latLon) {
            this.tourName = tourName;
            this.tourOwners = tourOwners;
            this.date = date;
            this.time = time;
            this.notes = notes;
            this.acceptedInvitees = acceptedInvitees;
            this.pendingInvitees = pendingInvitees;
            this.latLon = latLon;
        }





        public String getTourName() {
            return tourName;
        }

        public void setTourName(String tourName) {
            this.tourName = tourName;
        }

        public List<String> getTourOwners() {
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

        public List<String> getAcceptedInvitees() {
            return acceptedInvitees;
        }

        public void setAcceptedInvitees(ArrayList<String> acceptedInvitees) {
            this.acceptedInvitees = acceptedInvitees;
        }

        public List<String> getPendingInvitees() {
            return pendingInvitees;
        }

        public void setPendingInvitees(ArrayList<String> pendingInvitees) {
            this.pendingInvitees = pendingInvitees;
        }

        public String getLatLon() {
            return latLon;
        }

        public void setLatLon(String latLon) {
            this.latLon = latLon;
        }

        @Override
        public String toString() {
            return "Tour{" +
                    "tourName='" + tourName + '\'' +
                    ", tourOwners=" + tourOwners +
                    ", date='" + date + '\'' +
                    ", time='" + time + '\'' +
                    ", notes='" + notes + '\'' +
                    ", acceptedInvitees=" + acceptedInvitees +
                    ", pendingInvitees=" + pendingInvitees +
                    ", coordinates='" + latLon + '\'' +
                    '}';
        }

        /*
@       Override
        public int compareTo(Tour tour){
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm");
            try {
                Date thisDate = df.parse(this.date + " " + this.time);
                Date otherDate = df.parse(tour.date + " " + tour.time);
                return (int)(thisDate.getTime() - otherDate.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return 0;
        }

         */

    }

    @Override
    public String toString() {
        return "Tours{" +
                "acceptedToursIds=" + acceptedTourIds +
                ", pendingTourIds=" + pendingTourIds +
                '}';
    }
}
