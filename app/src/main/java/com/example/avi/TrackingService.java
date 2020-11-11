package com.example.avi;


import com.example.avi.ChatRoom.User;
import com.example.avi.Journals.Journal;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.location.LocationManager;
import android.os.IBinder;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.Manifest;
import android.location.Location;
import android.content.pm.PackageManager;
import android.app.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class TrackingService extends IntentService {

    private static final String TAG = TrackingService.class.getSimpleName();

    public static SharedPreferences sp;

    FusedLocationProviderClient client;

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    public User user;

    public static boolean currentlyRunning = false;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public TrackingService(String name) {
        super(name);
    }

    public TrackingService() {
        super("");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(!currentlyRunning) {
            currentlyRunning = true;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
        if (!currentlyRunning) {
            //buildNotification();
            loginToDatabase();
            requestLocationUpdates();
        }
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
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() != null) {
            final DocumentReference userDocRef = db.collection("users").document(mAuth.getUid());
            userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        Log.d("user", user.getId());
                    }
                }
            });
        }

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
        client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //If the user already gave permission to track their location
        if (permission == PackageManager.PERMISSION_GRANTED) {

            //...then request location updates
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    HashMap<String, String> location = new HashMap<>();
                    Location loc = locationResult.getLastLocation();
                    String lat = Double.toString(loc.getLatitude());
                    String lon = Double.toString(loc.getLongitude());
                    if (user != null) {
                        location.put("coordinates", lat + ", " + lon);
                        location.put("name", user.getName());
                        Log.d("tracking", "adding coordinates to db");
                        db.collection("tracking").document(user.getId()).set(location)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });;
                    } else {
                        if (mAuth.getCurrentUser() != null) {
                            final DocumentReference userDocRef = db.collection("users").document(mAuth.getUid());
                            userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    user = documentSnapshot.toObject(User.class);
                                }
                            });
                        }
                    }

                    //get all journals to loop through
                    final MyDBHandler dbHandler = new MyDBHandler(getApplicationContext(),
                            "journals.db", null, 1);
                    final MyDBHandler dbHandler_location = new MyDBHandler(getApplicationContext(),
                            "data_points.db", null, 1);
                    ArrayList<Journal> Journals = new ArrayList<Journal>();
                    Journals = dbHandler.getAllJournals();
                    Log.d("Tracking", "In location result callback");
                    for(Journal j : Journals)
                    {
                        if(j.start_recording)
                        {
                            dbHandler_location.add_to_data_points(j.name, (Double)loc.getLatitude(), (Double)loc.getLongitude());
                            Log.d("Tracking", "Storing coords in journals");
                            //String clean_email = LoginActivity.USER_EMAIL.replaceAll(".com", "");
                            String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
//                                    currentUser + "/journals/" + j.name + "/latlong");
                            Date timeStamp = new Date();
                            String s = timeStamp.toString();
//                            ref.child(s).setValue(lat + lon);
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