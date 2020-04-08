package com.example.avi;


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
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //If the user already gave permission to track their location
        if (permission == PackageManager.PERMISSION_GRANTED) {

            //...then request location updates
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    //TODO: now we can place the users current location into the database
                }
            }, null);
        }
    }
}