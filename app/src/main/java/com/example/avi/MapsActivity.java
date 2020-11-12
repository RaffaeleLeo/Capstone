package com.example.avi;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.example.avi.ChatRoom.ChatRoomActivity;
import com.example.avi.ChatRoom.User;
import com.example.avi.Journals.Journal;
import com.example.avi.Journals.JournalActivity;
import com.example.avi.Snapshot.SnapshotActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.example.avi.MyDBHandler;
import com.google.common.collect.Maps;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener, View.OnClickListener {

    private boolean inclineLocked = false;
    float incline = 0.0f;
    Button settings;
    Switch lockIncline;
    private GoogleMap mMap;
    private static final int PERMISSIONS_REQUEST = 100;
    private FusedLocationProviderClient mLocationClient;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    //compass stuff
    private ImageView compassButton;
    private float currentDegree = 0f;
    private SensorManager sensorManager;
    private boolean pressed = false;
    //x and y coordinates for button
    private float orgX;
    private float orgY;

    private float[] gravityData = new float[3];
    private float[] geomagneticData = new float[3];
    private boolean hasGravityData = false;
    private boolean hasGeomagneticData = false;
    private float rotationInDegrees;
    private String currentElevation;
    private float convertedDegrees = 0f;
    private Date now;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    public User user;

    //Strings for actual danger
    private HashMap<Integer, String> dangerDesc = new HashMap<Integer, String>() {{
        put(0, " (No rating)");
        put(1, " (Pockets of low danger)");
        put(2, " (Low danger)");
        put(3, " (Pockets of moderate danger)");
        put(4, " (Moderate danger)");
        put(5, " (Pockets of considerable danger)");
        put(6, " (Considerable danger)");
        put(7, " (Pockets of high danger)");
        put(8, " (High danger)");
        put(9, " (Pockets of extreme danger)");
        put(10, " (Extreme danger)");

    }};

    //For the path being displayed.
    private Iterable<LatLng> coordinates;
    private String journal_name;

    private MyDBHandler dbHandler;

    //dialog view
    View pop_up_view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Empty right now.
        coordinates = new ArrayList<LatLng>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        dbHandler = new MyDBHandler(getApplicationContext(), "danger.db", null, 1);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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
        //DANGER CODE STARTS HERE
        //Code to add current dangers to database
        //Usually would be the code commented out below, but
        //there are currently no forecasts so fill database
        //with dummy values for testing.
        Date currentTime = Calendar.getInstance().getTime();
        String temp = currentTime.toString();
        String split[] = temp.split(" ");
        String currDate = split[1] + " " + split[2] + " " + split[5];
        String lastDate = dbHandler.getDangerDate();
        dbHandler.clearDangerTable();
        for (int i = 0; i < 24; i++) {
            dbHandler.addToDanger(i, (24 - i) / 3, "tempurl", "None", "Salt Lake", currDate);
        }


//        if(!currDate.equals(lastDate)) {
//            dbHandler.clearDangerTable();
//            ArrayList<String> res = new ArrayList<String>();
//            PullDangerData danger = new PullDangerData();
//            danger.execute();
//            try {
//                res = danger.get();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            String url = res.get(24);
//            String overall = res.get(25);
//            String loc = res.get(26);
//
//            for (int i = 0; i < 24; i++) {
//                dbHandler.addToDanger(i, Integer.parseInt(res.get(i)), url, overall, loc, currDate);
//            }
//        }


        //get the users current location
        mLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        compassButton = findViewById(R.id.compass_button);
        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        compassButton.setOnClickListener(this);
        orgX = compassButton.getX();
        orgY = compassButton.getY();

        Intent intent = getIntent();

        setupTabLayout();
        Handler elevationHandler = new Handler();
        requestLocationUpdates(elevationHandler);

        if (intent.hasExtra("journal_name")) {
            this.journal_name = intent.getStringExtra("journal_name");
        } else {
            this.journal_name = null;
        }

        if (intent.hasExtra("FindMembers")) {

        }


        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            finish();
        }


        //Check whether this app has access to the location permission//
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);


        //If the location permission has been granted, then start the TrackerService
        if (permission == PackageManager.PERMISSION_GRANTED) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {

            //If the app doesn’t currently have access to the user’s location, then request access

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        final Button pop_up = findViewById(R.id.show_popup);

        pop_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater inflater = getLayoutInflater();
                pop_up_view = inflater.inflate(R.layout.sensors_layout, null);
                TextView altimeter = pop_up_view.findViewById(R.id.altimeter_value);
                if (currentElevation != null) {
                    altimeter.setText(currentElevation);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setView(pop_up_view);
                AlertDialog alertDialog = builder.create();
                builder.show();


            }
        });


        //compass stuff

        settings = findViewById(R.id.topBar).findViewById(R.id.settingsButton);
        TextView title = (TextView) findViewById(R.id.topBar).findViewById(R.id.pageTitle);
        title.setText("Maps");

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                intent.putExtra("PRIOR", 1);
                startActivity(intent);
            }
        });

        final Button snapshot = findViewById(R.id.snapshot_button);
        snapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MapsActivity.this, SnapshotActivity.class);
                    intent.putExtra("elevation", Float.parseFloat(currentElevation));
                    intent.putExtra("aspect", convertedDegrees);
                    intent.putExtra("PRIOR", 1);
                    startActivity(intent);
                } catch (NullPointerException e) {
                } catch (NumberFormatException e) {
                }
            }
        });
    }

    /**
     * Input a list of latitudes and longitudes. This method will make the map display a line from the beginning of the list to the end of the list.
     */
    public void createAndShowPathOnMap(List<Double> coords) {
        ArrayList<LatLng> newCoords = new ArrayList<LatLng>();
        for (int i = 0; i < coords.size(); i += 2) {
            newCoords.add(new LatLng(coords.get(i), coords.get(i + 1)));
        }
        this.coordinates = newCoords;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

        //get points from a journal and show it on the map
        final MyDBHandler dbHandler = new MyDBHandler(getApplicationContext(),
                "journals.db", null, 1);
        final MyDBHandler dbHandler_location = new MyDBHandler(getApplicationContext(),
                "data_points.db", null, 1);
        ArrayList<Journal> Journals = new ArrayList<Journal>();
        Journals = dbHandler.getAllJournals();

        for (Journal j : Journals) {
            if (j.name.equals(this.journal_name)) {
                createAndShowPathOnMap(dbHandler_location.getAllData(j.name));
            }
        }

        Polyline polylines = googleMap.addPolyline(new PolylineOptions().clickable(true).addAll(this.coordinates));


        String tourId = getIntent().getStringExtra("tourId");
        if (tourId != null) {
            getTourTrackingMembers(tourId);
        }

        //make the camera go to the users location
        //TODO: currently this will only go to the user once the app is opened, but won't move along with the user
        Task task = mLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                    mMap.animateCamera(cameraUpdate);
                }
            }
        });
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {

        //If the permission has been granted...//

        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    public void getTourTrackingMembers(String tourId) {
        if (user != null){
            db.collection("tours").document(tourId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Tours.Tour tour = documentSnapshot.toObject(Tours.Tour.class);
                    for (String accepted: tour.acceptedInvitees){

                            db.collection("users").whereEqualTo("email", accepted).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                        User person = doc.toObject(User.class);
                                        if (person != null){
                                            if (!user.getId().equals(person.getId())) {
                                                setUpToursTrackingListener(person.getId());
                                            }
                                        }

                                    }
                                }
                            });


                        }
                    }
            });
        }
    }

    public void setUpToursTrackingListener(String acceptedId){
        final DocumentReference docRef = db.collection("tracking").document(acceptedId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("memberTracking", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("memberTracking", "Current data: " + snapshot.getData());
                    Map<String, Object> tracking = snapshot.getData();
                    addGroupPositions(tracking.get("coordinates").toString(), tracking.get("name").toString());
                } else {
                    Log.d("memberTracking", "Current data: null");
                }
            }
        });
    }


    /**
     * sets up the tab layout at the bottom of the screen
     */
    private void setupTabLayout() {
        TabLayout.OnTabSelectedListener listener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 2) {
                    Intent intent = new Intent(MapsActivity.this, JournalActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 3) {
                    Intent intent = new Intent(MapsActivity.this, SocialMediaHomeActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 0) {
                    Intent intent = new Intent(MapsActivity.this, LiveUpdates.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        };
        TabLayout tabLayout = findViewById(R.id.TabLayout);

        tabLayout.addOnTabSelectedListener(listener);

        tabLayout.getTabAt(1).select();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, gravityData, 0, 3);
                hasGravityData = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, geomagneticData, 0, 3);
                hasGeomagneticData = true;
                break;
            default:
                return;
        }

        if (hasGravityData && hasGeomagneticData) {
            float identityMatrix[] = new float[9];
            float rotationMatrix[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(rotationMatrix, identityMatrix,
                    gravityData, geomagneticData);


            if (success) {
                float orientationMatrix[] = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientationMatrix);
                float rotationInRadians = orientationMatrix[0];
                rotationInDegrees = (float) Math.round(Math.toDegrees(rotationInRadians));

                float degree = rotationInDegrees;
                RotateAnimation ra = new RotateAnimation(currentDegree, -degree, compassButton.getX() + compassButton.getWidth() / 2,
                        compassButton.getY() + compassButton.getHeight() / 2);
                ra.setDuration(100);
                ra.setFillAfter(true);
                compassButton.startAnimation(ra);
                //DANGER CODE STARTS HERE
                //If we have an elevation, get it and the current degrees, and compute
                //the danger based at this location.
                //Converts degrees to 0-360 where 0 and 360 are N
                if (degree < 0) {
                    convertedDegrees = -degree;
                } else {
                    convertedDegrees = (float) (360.0 - degree);
                }
                if (currentElevation != null && !currentElevation.isEmpty()) {
                    int comp = getCompassLocation(Float.parseFloat(currentElevation), convertedDegrees);
                    if (pop_up_view != null) {
                        TextView danger = (TextView) pop_up_view.findViewById(R.id.Danger_value);
                        TextView dangerD = (TextView) pop_up_view.findViewById(R.id.Danger_explanation);
                        if (comp == -1) {
                            danger.setText("N/A");
                            danger.setTextColor(getColor(android.R.color.holo_green_dark));
                            dangerD.setText(" (Elevation below 5000)");
                        } else {
                            int d = dbHandler.getDangerAtLocation(comp);
                            danger.setText(Integer.toString(d));
                            if (d >= 7)
                                danger.setTextColor(getColor(android.R.color.holo_red_light));
                            else if (d >= 5)
                                danger.setTextColor(getColor(android.R.color.holo_orange_dark));
                            else if (d >= 3)
                                danger.setTextColor(getColor(android.R.color.holo_orange_light));
                            else
                                danger.setTextColor(getColor(android.R.color.holo_green_dark));
                            dangerD.setText(dangerDesc.get(d));
                        }
                    }

                }

                currentDegree = -degree;
                if (!inclineLocked)
                    incline = (float) Math.round(Math.abs(Math.toDegrees(orientationMatrix[1])));
                if (pop_up_view != null) {
                    TextView inclineTxt = (TextView) pop_up_view.findViewById(R.id.inclinometer_value);

                    inclineTxt.setText(Float.toString(incline));

                    lockIncline = (Switch) pop_up_view.findViewById(R.id.inclineSwitch);

                    lockIncline.setChecked(inclineLocked);


                    lockIncline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            inclineLocked = isChecked;
                        }
                    });


                }

                // do something with the rotation in degrees
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPause() {
        super.onPause();

        // to stop the listener and save battery
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        TabLayout tabLayout = findViewById(R.id.TabLayout);
        tabLayout.getTabAt(1).select();


        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onClick(View v) {
        if (!pressed) {
            compassButton.setScaleX(5);
            compassButton.setScaleY(5);
            compassButton.setX(this.getResources().getDisplayMetrics().widthPixels / 2 - compassButton.getWidth() / 2);
            compassButton.setY(this.getResources().getDisplayMetrics().heightPixels / 2 - compassButton.getHeight() / 2);
            pressed = true;
        } else {
            compassButton.setScaleX(1);
            compassButton.setScaleY(1);
            compassButton.setX(orgX);
            compassButton.setY(orgY);
            pressed = false;
        }


    }

    private void requestLocationUpdates(final Handler handler) {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            final int delay = 10000; //milliseconds
            //If the user already gave permission to track their location
            mLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onSuccess(Location location) {
                    String lat = Double.toString(location.getLatitude());
                    String lon = Double.toString(location.getLongitude());
                    //TODO: now we can place the users current location into the database
                    try {
                        ElevationData eleData = new ElevationData();
                        eleData.execute(lat, lon);
                        currentElevation = eleData.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
            handler.postDelayed(new Runnable() {
                public void run() {
                    mLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                String lat = Double.toString(location.getLatitude());
                                String lon = Double.toString(location.getLongitude());
                                //TODO: now we can place the users current location into the database
                                try {
                                    ElevationData eleData = new ElevationData();
                                    eleData.execute(lat, lon);
                                    currentElevation = eleData.get();
                                    if (pop_up_view != null) {
                                        TextView elevationText = (TextView) pop_up_view.findViewById(R.id.altimeter_value);
                                        elevationText.setText(currentElevation);
                                    }


                                    //DANGER CODE STARTS HERE
                                    //If we have an elevation, get it and the current degrees, and compute
                                    //the danger based at this location.
                                    if (currentElevation != null && !currentElevation.isEmpty()) {
                                        if (pop_up_view != null) {
                                            int comp = getCompassLocation(Float.parseFloat(currentElevation), convertedDegrees);
                                            TextView danger = (TextView) pop_up_view.findViewById(R.id.Danger_value);
                                            TextView dangerD = (TextView) pop_up_view.findViewById(R.id.Danger_explanation);

                                            if (comp == -1) {
                                                danger.setText("N/A");
                                                danger.setTextColor(getColor(android.R.color.holo_green_dark));

                                                dangerD.setText(" (Elevation below 5000)");
                                            } else {
                                                int d = dbHandler.getDangerAtLocation(comp);
                                                if (d >= 7)
                                                    danger.setTextColor(getColor(android.R.color.holo_red_light));
                                                else if (d >= 5)
                                                    danger.setTextColor(getColor(android.R.color.holo_orange_dark));
                                                else if (d >= 3)
                                                    danger.setTextColor(getColor(android.R.color.holo_orange_light));
                                                else
                                                    danger.setTextColor(getColor(android.R.color.holo_green_dark));
                                                danger.setText(Integer.toString(d));
                                                dangerD.setText(dangerDesc.get(d));
                                            }
                                        }
                                    }

                                } catch (Exception e) {

                                }
                            }
                        }
                    });


                    handler.postDelayed(this, delay);
                }
            }, delay);
            //...then request location updates

        }
    }

    private void addGroupPositions(String coordinates, String name) {
        String[] latLon = coordinates.split(", ");
        LatLng latLng = new LatLng(Double.parseDouble(latLon[0]), Double.parseDouble(latLon[1]));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.group_member_markers);
        Bitmap icon = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(icon);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        markerOptions.title(name);
        mMap.addMarker(markerOptions);
    }


    //Helper method to compute compass location in array
    //based on elevation and slope aspect.
    private int getCompassLocation(float elevation, float degrees) {
        int res = 0;
        if (elevation < 5000) {
            return -1;
        } else if (elevation <= 7000) {
            res = 16;
        } else if (elevation <= 8500) {
            res = 8;
        } else {
            res = 0;
        }

        if (degrees >= 22.5 && degrees < 67.5) {
            res = res + 1;
        } else if (degrees >= 67.5 && degrees < 112.5) {
            res = res + 2;
        } else if (degrees >= 112.5 && degrees < 157.5) {
            res = res + 3;
        } else if (degrees >= 157.5 && degrees < 202.5) {
            res = res + 4;
        } else if (degrees >= 202.5 && degrees < 247.5) {
            res = res + 5;
        } else if (degrees >= 247.5 && degrees < 292.5) {
            res = res + 6;
        } else if (degrees >= 292.5 && degrees < 337.5) {
            res = res + 7;
        } else {
            res = res + 0;
        }


        return res;
    }
}