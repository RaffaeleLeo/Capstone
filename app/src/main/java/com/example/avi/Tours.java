package com.example.avi;

import android.location.Location;

import java.util.Calendar;
import java.util.List;

public class Tours {

    private List<Tours.Tour> tours;

    static class Tour {
        public String tourName;
        public Calendar date;
        public String notes;
        public List<String> invitesPending;
        public List<String> invitesAccepted;
    }


}
