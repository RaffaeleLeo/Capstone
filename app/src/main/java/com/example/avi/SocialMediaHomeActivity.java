package com.example.avi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.avi.ChatRoom.ChatRoomActivity;
import com.example.avi.ChatRoom.User;
import com.example.avi.Journals.JournalActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class SocialMediaHomeActivity extends AppCompatActivity {

    Button settings;
    Button chat;
    Button friends;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private User user;
    private Tours tours;
    private Tours.Tour tour;
    private ArrayList<Tours.Tour> acceptedUserTours;
    private ArrayList<Tours.Tour> pendingUserTours;
    private LinearLayout toursLinearLayout;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        acceptedUserTours = new ArrayList<>();
        pendingUserTours = new ArrayList<>();

        settings = (Button) findViewById(R.id.gotoSettings);
        chat = (Button) findViewById(R.id.gotoChat);
        friends = (Button) findViewById(R.id.gotoFriends);
        toursLinearLayout = findViewById(R.id.tour_linear_layout);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SocialMediaHomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SocialMediaHomeActivity.this, ChatRoomActivity.class);
                startActivity(intent);
            }
        });

        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SocialMediaHomeActivity.this, FriendsActivity.class);
                startActivity(intent);
            }
        });
        setupTabLayout();

        final DocumentReference userDocRef = db.collection("users").document(mAuth.getUid());
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);
                Log.d("user", mAuth.getUid());

            }
        });
        final DocumentReference userToursDocRef = db.collection("userTours").document(mAuth.getUid());
        userToursDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                tours = documentSnapshot.toObject(Tours.class);
                if (tours != null) {
                    if (tours.getAcceptedTourIds().size() > 0) {
                        Log.d("user", tours.toString());
                        db.collection("tours").whereIn(FieldPath.documentId(), tours.getAcceptedTourIds()).get().addOnSuccessListener(
                                new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        acceptedUserTours.addAll(queryDocumentSnapshots.toObjects(Tours.Tour.class));
                                        Log.d("user", acceptedUserTours.get(0).toString());
                                        setupToursView("Accepted", acceptedUserTours);
                                    }
                                }
                        );
                    }
                    if (tours.getPendingTourIds().size() > 0) {
                        db.collection("tours").whereIn(FieldPath.documentId(), tours.getPendingTourIds()).get().addOnSuccessListener(
                                new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        pendingUserTours.addAll(queryDocumentSnapshots.toObjects(Tours.Tour.class));
                                        Log.d("user", pendingUserTours.get(0).toString());
                                        setupToursView("Pending", pendingUserTours);
                                    }
                                }
                        );
                    }
                }else {
                    db.collection("userTours").document(mAuth.getUid()).set(new Tours(new ArrayList<String>(), new ArrayList<String>()));
                }
            }
        });
    }

    private void setupToursView(String type, ArrayList<Tours.Tour> tours){
        switch(type) {
            case "Pending":
                for (int i = 0; i < tours.size(); i++){
                    View pendingTourBox = getLayoutInflater().inflate(R.layout.pending_tour_box, toursLinearLayout, false);
                    TextView tourName = pendingTourBox.findViewById(R.id.tour_name);
                    TextView tourDate = pendingTourBox.findViewById(R.id.date_text);
                    TextView tourTime = pendingTourBox.findViewById(R.id.time_text);
                    TextView tourNotes = pendingTourBox.findViewById(R.id.notes_text);

                    Tours.Tour tour = tours.get(i);
                    tourName.setText(tour.tourName);
                    tourDate.setText(tour.date);
                    tourTime.setText(tour.time);
                    tourNotes.setText(tour.notes);
                    toursLinearLayout.addView(pendingTourBox);
                }
                break;
            case "Accepted":
                for (int i = 0; i < tours.size(); i++){
                    View acceptedTourBox = getLayoutInflater().inflate(R.layout.tour_box, toursLinearLayout, false);
                    TextView tourName = acceptedTourBox.findViewById(R.id.tour_name);
                    TextView tourDate = acceptedTourBox.findViewById(R.id.date_text);
                    TextView tourTime = acceptedTourBox.findViewById(R.id.time_text);
                    TextView tourNotes = acceptedTourBox.findViewById(R.id.notes_text);

                    Tours.Tour tour = tours.get(i);
                    tourName.setText(tour.tourName);
                    tourDate.setText(tour.date);
                    tourTime.setText(tour.time);
                    tourNotes.setText(tour.notes);
                    toursLinearLayout.addView(acceptedTourBox);
                }
                break;
        }

    }

    private void setupTabLayout() {
        TabLayout.OnTabSelectedListener listener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 1) {
                    Intent intent = new Intent(SocialMediaHomeActivity.this, MapsActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 2) {
                    Intent intent = new Intent(SocialMediaHomeActivity.this, JournalActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                }else if (tab.getPosition() == 0){
                    Intent intent = new Intent(SocialMediaHomeActivity.this, LiveUpdates.class);

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

        tabLayout.getTabAt(3).select();
    }

}



