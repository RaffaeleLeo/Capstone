package com.example.avi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.avi.ChatRoom.ChatRoomActivity;
import com.example.avi.ChatRoom.User;
import com.example.avi.Journals.JournalActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.Write;
import com.google.firestore.v1.WriteResult;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

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
    private ImageButton addTourButtion;
    private ConstraintLayout rootLayout;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        acceptedUserTours = new ArrayList<>();
        pendingUserTours = new ArrayList<>();

        rootLayout = findViewById(R.id.social_media_constraint);
        settings = (Button) findViewById(R.id.gotoSettings);
        chat = (Button) findViewById(R.id.gotoChat);
        friends = (Button) findViewById(R.id.gotoFriends);
        toursLinearLayout = findViewById(R.id.tour_linear_layout);
        addTourButtion = findViewById(R.id.add_tour);
        setupAddTour();

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

    private void setupAddTour(){
        addTourButtion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View newTour = getLayoutInflater().inflate(R.layout.new_tour_box, rootLayout, false);
                rootLayout.addView(newTour);
                ImageButton saveButton = rootLayout.findViewById(R.id.saveButton);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String tourText;
                        String dateText;
                        String timeText;
                        String notesText;
                        String invitedText;

                        final View newTour = rootLayout.getChildAt(rootLayout.getChildCount()-1);
                        TextInputEditText newTourName = newTour.findViewById(R.id.tour_edit_text);
                        TextInputEditText newDate = newTour.findViewById(R.id.date_edit_text);
                        TextInputEditText newTourTime = newTour.findViewById(R.id.time_edit_text);
                        TextInputEditText newTourNotes = newTour.findViewById(R.id.notes_edit_text);
                        TextInputEditText newInvitedText = newTour.findViewById(R.id.invites_edit_text);


                        final View acceptedTourBox = getLayoutInflater().inflate(R.layout.tour_box, toursLinearLayout, false);
                        TextView tourName = acceptedTourBox.findViewById(R.id.tour_name);
                        TextView tourDate = acceptedTourBox.findViewById(R.id.date_text);
                        TextView tourTime = acceptedTourBox.findViewById(R.id.time_text);
                        TextView tourNotes = acceptedTourBox.findViewById(R.id.notes_text);
                        final TextView tourInvites = acceptedTourBox.findViewById(R.id.invites_text);
                        ImageButton settingsButton = acceptedTourBox.findViewById(R.id.edit_tour_button);
                        ImageButton invitesButton = acceptedTourBox.findViewById(R.id.invites_button);


                        tourText = newTourName.getText().toString().trim();
                        dateText = newDate.getText().toString().trim();
                        timeText = newTourTime.getText().toString().trim();
                        notesText = newTourNotes.getText().toString().trim();
                        invitedText = newInvitedText.getText().toString().trim();

                        if (tourText.isEmpty()) tourText = "No Name";
                        if (dateText.isEmpty()) dateText = "No Date";
                        if (timeText.isEmpty()) timeText = "No Time";
                        if (notesText.isEmpty()) notesText = "No Notes";
                        if (invitedText.isEmpty()) invitedText = "No Invites";

                        ViewGroup.LayoutParams params = tourInvites.getLayoutParams();
                        params.height = 0;
                        tourInvites.setLayoutParams(params);

                        tourName.setText(tourText);
                        tourDate.setText(dateText);
                        tourTime.setText(timeText);
                        tourNotes.setText(notesText);
                        tourInvites.setText(invitedText);
                        ArrayList<String> tourOwners = new ArrayList<>();
                        ArrayList<String> acceptedList = new ArrayList<>();
                        ArrayList<String> pendingList = new ArrayList<>(Arrays.asList(invitedText.split("\n")));
                        ArrayList<String> declinedList = new ArrayList<>();
                        tourOwners.add(mAuth.getUid());
                        acceptedList.add(user.getEmail());
                        final Tours.Tour tour = new Tours.Tour(tourText, tourOwners, dateText, timeText, notesText, acceptedList, pendingList, declinedList, "placeholder", "placeholder");
                        db.collection("tours").add(tour).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                final String docId = documentReference.getId();
                                Log.d("docId", docId);
                                db.collection("userTours").document(mAuth.getUid()).update("acceptedTourIds", FieldValue.arrayUnion(docId));
                                db.collection("users").whereIn("email", tour.pendingInvitees).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                            final String userId = document.getId();
                                            Log.d("addTour", userId);
                                            Log.d("userId", document.getId());
                                            db.collection("userTours").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    Tours tours = documentSnapshot.toObject(Tours.class);
                                                    if (tours != null){
                                                        tours.getPendingTourIds().add(docId);
                                                        db.collection("userTours").document(userId).set(tours);
                                                        Log.d("addTour", userId);
                                                    }else {
                                                        ArrayList<String> pending = new ArrayList<>();
                                                        pending.add(docId);
                                                        Tours newTours = new Tours(new ArrayList<String>(), pending);
                                                        db.collection("userTours").document(userId).set(newTours);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                                acceptedTourBox.setTag(docId);
                                toursLinearLayout.addView(acceptedTourBox);
                            }

                        });
                        rootLayout.removeView(newTour);
                        setupTourInvitesButton(invitesButton, tourInvites);
                        settingsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                    }
                });
                ImageButton discardButton = rootLayout.findViewById(R.id.discardButton);
                discardButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View newTour = rootLayout.getChildAt(rootLayout.getChildCount()-1);
                        rootLayout.removeView(newTour);
                    }
                });
            }
        });
    }


    private void setupTourInvitesButton(ImageButton invitesButton, final TextView tourInvites){
        invitesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tourInvites.getVisibility() == View.INVISIBLE){
                    tourInvites.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams params = tourInvites.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    tourInvites.setLayoutParams(params);
                }else {
                    tourInvites.setVisibility(View.INVISIBLE);
                    ViewGroup.LayoutParams params = tourInvites.getLayoutParams();
                    params.height = 0;
                    tourInvites.setLayoutParams(params);
                }
            }
        });
    }
    private void setupToursView(String type, ArrayList<Tours.Tour> tours){
        switch(type) {
            case "Pending":
                ArrayList<String> pendingTourIds = new ArrayList<>();
                pendingTourIds.addAll(this.tours.getPendingTourIds());
                for (int i = 0; i < tours.size(); i++){
                    final View pendingTourBox = getLayoutInflater().inflate(R.layout.pending_tour_box, toursLinearLayout, false);
                    TextView tourName = pendingTourBox.findViewById(R.id.tour_name);
                    TextView tourDate = pendingTourBox.findViewById(R.id.date_text);
                    TextView tourTime = pendingTourBox.findViewById(R.id.time_text);
                    TextView tourNotes = pendingTourBox.findViewById(R.id.notes_text);
                    final TextView tourInvites = pendingTourBox.findViewById(R.id.invites_text);
                    ImageButton invitesButton = pendingTourBox.findViewById(R.id.invites_button);
                    ImageButton acceptButton = pendingTourBox.findViewById(R.id.acceptButton);


                    tourInvites.setVisibility(View.INVISIBLE);
                    ViewGroup.LayoutParams params = tourInvites.getLayoutParams();
                    params.height = 0;
                    tourInvites.setLayoutParams(params);

                    setupTourInvitesButton(invitesButton, tourInvites);
                    Tours.Tour tour = tours.get(i);
                    pendingTourBox.setTag(pendingTourIds.get(i));
                    acceptButton.setTag(pendingTourBox);
                    acceptButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            View pendingTourBox = (View) view.getTag();
                            String tourId = (String) pendingTourBox.getTag();
                            int boxIndex = toursLinearLayout.indexOfChild(pendingTourBox);
                            TextView pendingTourName = pendingTourBox.findViewById(R.id.tour_name);
                            TextView pendingTourDate = pendingTourBox.findViewById(R.id.date_text);
                            TextView pendingTourTime = pendingTourBox.findViewById(R.id.time_text);
                            TextView pendingTourNotes = pendingTourBox.findViewById(R.id.notes_text);
                            final TextView pendingTourInvites = pendingTourBox.findViewById(R.id.invites_text);

                            View acceptedTourBox = getLayoutInflater().inflate(R.layout.tour_box, toursLinearLayout, false);

                            TextView acceptedTourName = acceptedTourBox.findViewById(R.id.tour_name);
                            TextView acceptedTourDate = acceptedTourBox.findViewById(R.id.date_text);
                            TextView acceptedTourTime = acceptedTourBox.findViewById(R.id.time_text);
                            TextView accepetedTourNotes = acceptedTourBox.findViewById(R.id.notes_text);

                            final TextView acceptedTourInvites = acceptedTourBox.findViewById(R.id.invites_text);
                            ImageButton acceptedInvitesButton = acceptedTourBox.findViewById(R.id.invites_button);



                            setupTourInvitesButton(acceptedInvitesButton, acceptedTourInvites);
                            acceptedTourBox.setTag(view.getTag());
                            acceptedTourName.setText(pendingTourName.getText().toString());
                            acceptedTourDate.setText(pendingTourDate.getText().toString());
                            acceptedTourTime.setText(pendingTourTime.getText().toString());
                            accepetedTourNotes.setText(pendingTourNotes.getText().toString());
                            acceptedTourInvites.setText(pendingTourInvites.getText().toString());

                            acceptedTourInvites.setVisibility(View.INVISIBLE);
                            ViewGroup.LayoutParams params = acceptedTourInvites.getLayoutParams();
                            params.height = 0;
                            acceptedTourInvites.setLayoutParams(params);

                            toursLinearLayout.removeViewAt(boxIndex);
                            toursLinearLayout.addView(acceptedTourBox, boxIndex);
                            db.collection("userTours").document(mAuth.getUid()).update("acceptedTourIds", FieldValue.arrayUnion(tourId));
                            db.collection("userTours").document(mAuth.getUid()).update("pendingTourIds", FieldValue.arrayRemove(tourId));
                            db.collection("tours").document(tourId).update("acceptedInvitees", FieldValue.arrayUnion(user.getEmail()));
                            db.collection("tours").document(tourId).update("pendingInvitees", FieldValue.arrayRemove(user.getEmail()));
                        }
                    });

                    tourName.setText(tour.tourName);
                    tourDate.setText(tour.date);
                    tourTime.setText(tour.time);
                    tourNotes.setText(tour.notes);
                    toursLinearLayout.addView(pendingTourBox);
                }
                break;
            case "Accepted":
                ArrayList<String> acceptedTourIds = new ArrayList<>();
                acceptedTourIds.addAll(this.tours.getAcceptedTourIds());
                for (int i = 0; i < tours.size(); i++){
                    View acceptedTourBox = getLayoutInflater().inflate(R.layout.tour_box, toursLinearLayout, false);
                    TextView tourName = acceptedTourBox.findViewById(R.id.tour_name);
                    TextView tourDate = acceptedTourBox.findViewById(R.id.date_text);
                    TextView tourTime = acceptedTourBox.findViewById(R.id.time_text);
                    TextView tourNotes = acceptedTourBox.findViewById(R.id.notes_text);

                    final TextView tourInvites = acceptedTourBox.findViewById(R.id.invites_text);
                    ImageButton invitesButton = acceptedTourBox.findViewById(R.id.invites_button);

                    tourInvites.setVisibility(View.INVISIBLE);
                    ViewGroup.LayoutParams params = tourInvites.getLayoutParams();
                    params.height = 0;
                    tourInvites.setLayoutParams(params);

                    setupTourInvitesButton(invitesButton, tourInvites);
                    Tours.Tour tour = tours.get(i);
                    acceptedTourBox.setTag(acceptedTourIds.get(i));
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



