package com.example.avi;


import com.example.avi.Journals.Journal;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.content.BroadcastReceiver;
import android.content.Context;

import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;
import android.os.IBinder;
import android.content.Intent;
import android.util.Log;
import android.Manifest;
import android.location.Location;
import android.content.pm.PackageManager;
import android.app.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class TrackingService extends Service {

    private static final String TAG = TrackingService.class.getSimpleName();

    public static SharedPreferences sp;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
        //buildNotification();
        loginToDatabase();
        requestLocationUpdates();
    }


    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Unregister the BroadcastReceiver when the notification is tapped//

            unregisterReceiver(stopReceiver);

            //Stop the Service//

            stopSelf();
        }
    };

    private void loginToDatabase() {

        // TODO: use this method to ensure the logged in user in within the database
    }

    /**
     * Initiate the request to track the device's location
     */
    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();

        //How often the app will track the users location
        request.setInterval(10000);


        //Try to get as accurate of an approximation as we can
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //If the user already gave permission to track their location
        if (permission == PackageManager.PERMISSION_GRANTED) {

            //...then request location updates
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location loc = locationResult.getLastLocation();
                    String lat = Double.toString(loc.getLatitude());
                    String lon = Double.toString(loc.getLongitude());

                    //get all journals to loop through
                    final MyDBHandler dbHandler = new MyDBHandler(getApplicationContext(),
                            "journals.db", null, 1);
                    final MyDBHandler dbHandler_location = new MyDBHandler(getApplicationContext(),
                            "data_points.db", null, 1);
                    ArrayList<Journal> Journals = new ArrayList<Journal>();
                    Journals = dbHandler.getAllJournals();

                    for(Journal j : Journals)
                    {
                        if(j.start_recording)
                        {
                            dbHandler_location.add_to_data_points(j.name, (Double)loc.getLatitude(), (Double)loc.getLongitude());
                        }
                    }

                    try {
                        ElevationData eleData = new ElevationData();
                        eleData.execute(lat, lon);
                        eleData.get();
                    } catch (Exception e){

                    }
                }
            }, null);
        }
    }


}