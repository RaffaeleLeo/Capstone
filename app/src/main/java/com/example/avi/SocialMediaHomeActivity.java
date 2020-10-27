package com.example.avi;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.avi.ChatRoom.ChatRoomActivity;
import com.example.avi.ChatRoom.User;
import com.example.avi.Journals.JournalActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class SocialMediaHomeActivity extends AppCompatActivity {

    Button settings;
    Button chat;
    Button friends;
    Boolean behaveNormallyWhenBackIsPressed;

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


    @Override
    protected void onResume() {
        super.onResume();
        TabLayout tabLayout = findViewById(R.id.TabLayout);
        tabLayout.getTabAt(3).select();
    }

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        behaveNormallyWhenBackIsPressed = true;
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
                intent.putExtra("PRIOR", 3);
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
                    setUpDocListeners();
                } else {
                    db.collection("userTours").document(mAuth.getUid()).set(new Tours(new ArrayList<String>(), new ArrayList<String>()));
                    setUpDocListeners();
                }
            }
        });
    }

    private void setupAddTour() {
        addTourButtion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                behaveNormallyWhenBackIsPressed = false;
                final View newTour = getLayoutInflater().inflate(R.layout.new_tour_box, rootLayout, false);
                rootLayout.addView(newTour);

                ImageButton saveButton = rootLayout.findViewById(R.id.saveButton);
                final TextInputEditText newTourName = newTour.findViewById(R.id.tour_edit_text);
                final TextInputEditText newDate = newTour.findViewById(R.id.date_edit_text);
                final TextInputEditText newTourTime = newTour.findViewById(R.id.time_edit_text);
                final TextInputEditText newTourNotes = newTour.findViewById(R.id.notes_edit_text);
                final TextInputEditText newInvitedText = newTour.findViewById(R.id.invites_edit_text);

                setUpDateListeners(newDate);
                setUpTimeListeners(newTourTime);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        behaveNormallyWhenBackIsPressed = true;
                        String tourText;
                        String dateText;
                        String timeText;
                        String notesText;
                        String invitedText;



                        final View acceptedTourBox = getLayoutInflater().inflate(R.layout.tour_box, toursLinearLayout, false);
                        TextView tourName = acceptedTourBox.findViewById(R.id.tour_name);
                        TextView tourDate = acceptedTourBox.findViewById(R.id.date_text);
                        TextView tourTime = acceptedTourBox.findViewById(R.id.time_text);
                        TextView tourNotes = acceptedTourBox.findViewById(R.id.notes_text);
                        final TextView tourInvites = acceptedTourBox.findViewById(R.id.invites_text);
                        ImageButton editTourButton = acceptedTourBox.findViewById(R.id.edit_tour_button);
                        ImageButton invitesButton = acceptedTourBox.findViewById(R.id.invites_button);
                        final ImageButton deleteButton = acceptedTourBox.findViewById(R.id.delete_button);
                        final ImageButton editButton = acceptedTourBox.findViewById(R.id.edit_button);
                        deleteButton.setTag(acceptedTourBox);
                        editTourButton.setTag(acceptedTourBox);
                        editButton.setTag(acceptedTourBox);
                        editTourButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (deleteButton.getVisibility() == View.GONE) {
                                    deleteButton.setVisibility(View.VISIBLE);
                                    editButton.setVisibility(View.VISIBLE);
                                } else {
                                    deleteButton.setVisibility(View.GONE);
                                    editButton.setVisibility(View.GONE);
                                }
                            }
                        });
                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                View tourBox = (View) view.getTag();
                                final String tourId = (String) tourBox.getTag();
                                int boxIndex = toursLinearLayout.indexOfChild(tourBox);
                                toursLinearLayout.removeViewAt(boxIndex);
                                db.collection("userTours").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            String docId = documentSnapshot.getId();

                                            db.collection("userTours").document(docId).update("acceptedTourIds", FieldValue.arrayRemove(tourId));
                                            db.collection("userTours").document(docId).update("pendingTourIds", FieldValue.arrayRemove(tourId));
                                        }
                                    }
                                });
                                db.collection("tours").document(tourId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("dbDelete", "Deletion successful " + tourId);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("dbDelete", "Deletion unsuccessful " + tourId);
                                    }
                                });
                            }
                        });
                        //Set up edit button on tours
                        setUpEditTourInfoButton(editButton);
                        tourText = newTourName.getText().toString().trim();
                        dateText = newDate.getText().toString().trim();
                        timeText = newTourTime.getText().toString().trim();
                        notesText = newTourNotes.getText().toString().trim();
                        invitedText = newInvitedText.getText().toString().trim();

                        tourInvites.setVisibility(View.GONE);

                        tourName.setText(tourText);
                        tourDate.setText(dateText);
                        tourTime.setText(timeText);
                        tourNotes.setText(notesText);
                        tourInvites.setText(invitedText);
                        ArrayList<String> tourOwners = new ArrayList<>();
                        ArrayList<String> acceptedList = new ArrayList<>();
                        ArrayList<String> pendingList = new ArrayList<>(Arrays.asList(invitedText.split("\n")));
                        tourOwners.add(mAuth.getUid());
                        acceptedList.add(user.getEmail());
                        final Tours.Tour tour = new Tours.Tour(tourText, tourOwners, dateText, timeText, notesText, acceptedList, pendingList, "placeholder", "placeholder");
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
                                                    if (tours != null) {
                                                        tours.getPendingTourIds().add(docId);
                                                        db.collection("userTours").document(userId).set(tours);
                                                        Log.d("addTour", userId);
                                                    } else {
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
                    }
                });
                ImageButton discardButton = rootLayout.findViewById(R.id.discardButton);
                discardButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        behaveNormallyWhenBackIsPressed = true;
                        View newTour = rootLayout.getChildAt(rootLayout.getChildCount() - 1);
                        rootLayout.removeView(newTour);
                    }
                });



            }
        });
    }

    private void setUpEditTourInfoButton(ImageButton editButton){
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                behaveNormallyWhenBackIsPressed = false;
                final View newTour = getLayoutInflater().inflate(R.layout.edit_tour_box, rootLayout, false);

                final View tourBox = (View) view.getTag();
                final String tourId = (String) tourBox.getTag();

                ImageButton saveButton = newTour.findViewById(R.id.saveButton);
                final TextInputEditText newTourName = newTour.findViewById(R.id.tour_edit_text);
                final TextInputEditText newDate = newTour.findViewById(R.id.date_edit_text);
                final TextInputEditText newTourTime = newTour.findViewById(R.id.time_edit_text);
                final TextInputEditText newTourNotes = newTour.findViewById(R.id.notes_edit_text);
                final TextInputEditText newInvitedText = newTour.findViewById(R.id.invites_edit_text);

                setUpDateListeners(newDate);
                setUpTimeListeners(newTourTime);

                final TextView tourName = tourBox.findViewById(R.id.tour_name);
                final TextView tourDate = tourBox.findViewById(R.id.date_text);
                final TextView tourTime = tourBox.findViewById(R.id.time_text);
                final TextView tourNotes = tourBox.findViewById(R.id.notes_text);
                final TextView tourInvites = tourBox.findViewById(R.id.invites_text);
                newTourName.setText(tourName.getText());
                newDate.setText(tourDate.getText());
                newTourTime.setText(tourTime.getText());
                newTourNotes.setText(tourNotes.getText());
                newInvitedText.setText(tourInvites.getText());
                rootLayout.addView(newTour);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        behaveNormallyWhenBackIsPressed = true;
                        String tourText;
                        String dateText;
                        String timeText;
                        String notesText;
                        String invitedText;

                        tourText = newTourName.getText().toString().trim();
                        dateText = newDate.getText().toString().trim();
                        timeText = newTourTime.getText().toString().trim();
                        notesText = newTourNotes.getText().toString().trim();
                        invitedText = newInvitedText.getText().toString().trim();


                        tourInvites.setVisibility(View.GONE);

                        tourName.setText(tourText);
                        tourDate.setText(dateText);
                        tourTime.setText(timeText);
                        tourNotes.setText(notesText);
                        tourInvites.setText(invitedText);


                        //Data base changes
                        final String finalTourText = tourText;
                        final ArrayList<String> invitees = new ArrayList<>(Arrays.asList(invitedText.split("\n")));
                        final String finalDateText = dateText;
                        final String finalTimeText = timeText;
                        final String finalNotesText = notesText;
                        db.collection("tours").document(tourId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Tours.Tour tourEdit = documentSnapshot.toObject(Tours.Tour.class);
                                ArrayList<String> removeInvitees = new ArrayList<>();
                                ArrayList<String> removeAccepted = new ArrayList<>();
                                ArrayList<String> removePending = new ArrayList<>();
                                ArrayList<String> addPending = new ArrayList<>();
                                addPending.addAll(invitees);
                                for(String person : tourEdit.acceptedInvitees){
                                    if (!invitees.contains(person) && !person.equals(user.getEmail())){
                                        removeInvitees.add(person);
                                        removeAccepted.add(person);
                                    }else {
                                        addPending.remove(person);
                                    }
                                }
                                tourEdit.acceptedInvitees.removeAll(removeAccepted);
                                for (String person : tourEdit.pendingInvitees){
                                    if (!invitees.contains(person)){
                                        removeInvitees.add(person);
                                        removePending.add(person);
                                    } else {
                                        addPending.remove(person);
                                    }
                                }
                                tourEdit.pendingInvitees.removeAll(removePending);
                                tourEdit.pendingInvitees.addAll(addPending);
                                tourEdit.tourName = finalTourText;
                                tourEdit.date = finalDateText;
                                tourEdit.time = finalTimeText;
                                tourEdit.notes = finalNotesText;

                                db.collection("tours").document(tourId).set(tourEdit).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("TourFailure", "Edit Tour Failed in database");
                                    }
                                });
                                if (removeInvitees.size() > 0) {
                                    db.collection("users").whereIn("email", removeInvitees).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                final String userId = document.getId();
                                                Log.d("addTour", userId);
                                                Log.d("userId", document.getId());
                                                db.collection("userTours").document(userId).update("acceptedTourIds", FieldValue.arrayRemove(tourId));
                                                db.collection("userTours").document(userId).update("pendingTourIds", FieldValue.arrayRemove(tourId));
                                            }
                                        }
                                    });
                                }
                                if(addPending.size() > 0){
                                    db.collection("users").whereIn("email", addPending).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                final String userId = document.getId();
                                                Log.d("addTour", userId);
                                                Log.d("userId", document.getId());
                                                db.collection("userTours").document(userId).update("pendingTourIds", FieldValue.arrayUnion(tourId));
                                            }
                                        }
                                    });
                                }
                            }
                        });

                        rootLayout.removeView(newTour);
                    }

                });
                ImageButton discardButton = rootLayout.findViewById(R.id.discardButton);
                discardButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        behaveNormallyWhenBackIsPressed = true;
                        View newTour = rootLayout.getChildAt(rootLayout.getChildCount() - 1);
                        rootLayout.removeView(newTour);
                    }
                });
                                /*
                                db.collection("userTours").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            String docId = documentSnapshot.getId();

                                            db.collection("userTours").document(docId).update("acceptedTourIds", FieldValue.arrayRemove(tourId));
                                            db.collection("userTours").document(docId).update("pendingTourIds", FieldValue.arrayRemove(tourId));
                                        }
                                    }
                                });
                                db.collection("tours").document(tourId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("dbDelete", "Deletion successful " + tourId);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("dbDelete", "Deletion unsuccessful " + tourId);

                                    }
                                });
                        */
            }
        });

    }


    private void setupTourInvitesButton(ImageButton invitesButton, final TextView tourInvites) {
        invitesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tourInvites.getVisibility() == View.GONE) {
                    tourInvites.setVisibility(View.VISIBLE);
                } else {
                    tourInvites.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupToursView(String type, ArrayList<Tours.Tour> tours) {
        switch (type) {
            case "Pending":
                ArrayList<String> pendingTourIds = new ArrayList<>();
                pendingTourIds.addAll(this.tours.getPendingTourIds());
                for (int i = 0; i < tours.size(); i++) {
                    final View pendingTourBox = getLayoutInflater().inflate(R.layout.pending_tour_box, toursLinearLayout, false);
                    TextView tourName = pendingTourBox.findViewById(R.id.tour_name);
                    TextView tourDate = pendingTourBox.findViewById(R.id.date_text);
                    TextView tourTime = pendingTourBox.findViewById(R.id.time_text);
                    TextView tourNotes = pendingTourBox.findViewById(R.id.notes_text);
                    final TextView tourInvites = pendingTourBox.findViewById(R.id.invites_text);
                    ImageButton invitesButton = pendingTourBox.findViewById(R.id.invites_button);
                    ImageButton acceptButton = pendingTourBox.findViewById(R.id.acceptButton);
                    ImageButton declineButton = pendingTourBox.findViewById(R.id.declineButton);


                    tourInvites.setVisibility(View.GONE);

                    setupTourInvitesButton(invitesButton, tourInvites);
                    Tours.Tour tour = tours.get(i);
                    pendingTourBox.setTag(pendingTourIds.get(i));
                    acceptButton.setTag(pendingTourBox);
                    declineButton.setTag(pendingTourBox);
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
                            final ImageButton editTourButton = acceptedTourBox.findViewById(R.id.edit_tour_button);
                            final ImageButton deleteButton = acceptedTourBox.findViewById(R.id.delete_button);
                            acceptedTourBox.setTag(pendingTourBox.getTag());
                            deleteButton.setTag(acceptedTourBox);
                            editTourButton.setTag(acceptedTourBox);

                            final TextView acceptedTourInvites = acceptedTourBox.findViewById(R.id.invites_text);
                            ImageButton acceptedInvitesButton = acceptedTourBox.findViewById(R.id.invites_button);
                            deleteButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    View tourBox = (View) view.getTag();
                                    String tourId = (String) tourBox.getTag();
                                    int boxIndex = toursLinearLayout.indexOfChild(tourBox);
                                    toursLinearLayout.removeViewAt(boxIndex);
                                    db.collection("userTours").document(user.getId()).update("acceptedTourIds", FieldValue.arrayRemove(tourId));
                                    db.collection("tours").document(tourId).update("acceptedInvitees", FieldValue.arrayRemove(user.getEmail()));
                                }
                            });
                            editTourButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (deleteButton.getVisibility() == View.GONE) {
                                        deleteButton.setVisibility(View.VISIBLE);
                                    } else {
                                        deleteButton.setVisibility(View.GONE);
                                    }
                                }
                            });

                            setupTourInvitesButton(acceptedInvitesButton, acceptedTourInvites);
                            acceptedTourName.setText(pendingTourName.getText().toString());
                            acceptedTourDate.setText(pendingTourDate.getText().toString());
                            acceptedTourTime.setText(pendingTourTime.getText().toString());
                            accepetedTourNotes.setText(pendingTourNotes.getText().toString());
                            acceptedTourInvites.setText(pendingTourInvites.getText().toString());

                            acceptedTourInvites.setVisibility(View.GONE);

                            toursLinearLayout.removeViewAt(boxIndex);
                            toursLinearLayout.addView(acceptedTourBox, boxIndex);
                            db.collection("userTours").document(mAuth.getUid()).update("acceptedTourIds", FieldValue.arrayUnion(tourId));
                            db.collection("userTours").document(mAuth.getUid()).update("pendingTourIds", FieldValue.arrayRemove(tourId));
                            db.collection("tours").document(tourId).update("acceptedInvitees", FieldValue.arrayUnion(user.getEmail()));
                            db.collection("tours").document(tourId).update("pendingInvitees", FieldValue.arrayRemove(user.getEmail()));
                        }
                    });

                    declineButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            View pendingTourBox = (View) view.getTag();
                            String tourId = (String) pendingTourBox.getTag();
                            int boxIndex = toursLinearLayout.indexOfChild(pendingTourBox);
                            toursLinearLayout.removeViewAt(boxIndex);
                            db.collection("userTours").document(mAuth.getUid()).update("pendingTourIds", FieldValue.arrayRemove(tourId));
                            db.collection("tours").document(tourId).update("pendingInvitees", FieldValue.arrayRemove(user.getEmail()));
                        }
                    });
                    tourName.setText(tour.tourName);
                    tourDate.setText(tour.date);
                    tourTime.setText(tour.time);
                    tourNotes.setText(tour.notes);
                    StringBuilder sb = new StringBuilder();
                    for (String person : tour.acceptedInvitees) {
                            sb.append(person + "\n");
                    }
                    for (String person : tour.pendingInvitees) {
                        if (!person.equals(user.getEmail())) {
                            sb.append(person + "\n");
                        }
                    }
                    tourInvites.setText(sb.toString());
                    toursLinearLayout.addView(pendingTourBox);
                }
                break;
            case "Accepted":
                ArrayList<String> acceptedTourIds = new ArrayList<>();
                acceptedTourIds.addAll(this.tours.getAcceptedTourIds());
                for (int i = 0; i < tours.size(); i++) {
                    final ArrayList<String> owners = new ArrayList<>(tours.get(i).tourOwners);
                    View acceptedTourBox = getLayoutInflater().inflate(R.layout.tour_box, toursLinearLayout, false);
                    acceptedTourBox.setTag(acceptedTourIds.get(i));
                    final ImageButton editTourButton = acceptedTourBox.findViewById(R.id.edit_tour_button);
                    final ImageButton deleteButton = acceptedTourBox.findViewById(R.id.delete_button);
                    deleteButton.setTag(acceptedTourBox);
                    editTourButton.setTag(acceptedTourBox);
                    final ImageButton editButton = acceptedTourBox.findViewById(R.id.edit_button);
                    editButton.setTag(acceptedTourBox);
                    if (owners.contains(user.getId())) {
                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                View tourBox = (View) view.getTag();
                                final String tourId = (String) tourBox.getTag();
                                int boxIndex = toursLinearLayout.indexOfChild(tourBox);
                                toursLinearLayout.removeViewAt(boxIndex);
                                db.collection("userTours").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            String docId = documentSnapshot.getId();

                                            db.collection("userTours").document(docId).update("acceptedTourIds", FieldValue.arrayRemove(tourId));
                                            db.collection("userTours").document(docId).update("pendingTourIds", FieldValue.arrayRemove(tourId));
                                        }
                                    }
                                });
                                db.collection("tours").document(tourId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("dbDelete", "Deletion successful " + tourId);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("dbDelete", "Deletion unsuccessful " + tourId);
                                    }
                                });
                            }
                        });
                        editTourButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (deleteButton.getVisibility() == View.GONE) {
                                    deleteButton.setVisibility(View.VISIBLE);
                                    editButton.setVisibility(View.VISIBLE);
                                } else {
                                    deleteButton.setVisibility(View.GONE);
                                    editButton.setVisibility(View.GONE);
                                }
                            }
                        });

                        setUpEditTourInfoButton(editButton);
                    } else {
                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                View tourBox = (View) view.getTag();
                                String tourId = (String) tourBox.getTag();
                                int boxIndex = toursLinearLayout.indexOfChild(tourBox);
                                toursLinearLayout.removeViewAt(boxIndex);
                                db.collection("userTours").document(user.getId()).update("acceptedTourIds", FieldValue.arrayRemove(tourId));
                                db.collection("tours").document(tourId).update("acceptedInvitees", FieldValue.arrayRemove(user.getEmail()));
                            }
                        });
                        editTourButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (deleteButton.getVisibility() == View.GONE) {
                                    deleteButton.setVisibility(View.VISIBLE);
                                } else {
                                    deleteButton.setVisibility(View.GONE);
                                }
                            }
                        });
                    }

                    TextView tourName = acceptedTourBox.findViewById(R.id.tour_name);
                    TextView tourDate = acceptedTourBox.findViewById(R.id.date_text);
                    TextView tourTime = acceptedTourBox.findViewById(R.id.time_text);
                    TextView tourNotes = acceptedTourBox.findViewById(R.id.notes_text);

                    final TextView tourInvites = acceptedTourBox.findViewById(R.id.invites_text);
                    ImageButton invitesButton = acceptedTourBox.findViewById(R.id.invites_button);

                    tourInvites.setVisibility(View.GONE);

                    setupTourInvitesButton(invitesButton, tourInvites);
                    Tours.Tour tour = tours.get(i);
                    tourName.setText(tour.tourName);
                    tourDate.setText(tour.date);
                    tourTime.setText(tour.time);
                    tourNotes.setText(tour.notes);
                    StringBuilder sb = new StringBuilder();
                    for (String person : tour.acceptedInvitees) {
                        if (!person.equals(user.getEmail())) {
                            sb.append(person + "\n");
                        }
                    }
                    for (String person : tour.pendingInvitees) {
                        sb.append(person + "\n");
                    }
                    tourInvites.setText(sb.toString());
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
                } else if (tab.getPosition() == 0) {
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

    @Override
    public void onBackPressed() {
        if (behaveNormallyWhenBackIsPressed)
            super.onBackPressed();
        else {
            behaveNormallyWhenBackIsPressed = true;
            View newTour = rootLayout.getChildAt(rootLayout.getChildCount() - 1);
            rootLayout.removeView(newTour);
        }
    }

    public void setUpDateListeners(final TextInputEditText editDate) {
        final Calendar myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                editDate.setText(sdf.format(myCalendar.getTime()));
            }

        };

        editDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(SocialMediaHomeActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    public void setUpTimeListeners(final TextInputEditText editTime){
        editTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(SocialMediaHomeActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        editTime.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
    }

    public void setUpDocListeners () {
        final DocumentReference docRef = db.collection("userTours").document(mAuth.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TourDocListener", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                   Tours newToursData = snapshot.toObject(Tours.class);
                   ArrayList<String> currentPending = new ArrayList<>(tours.getPendingTourIds());
                   ArrayList<String> pendingToursToAdd = new ArrayList<>();
                   ArrayList<String> acceptedTours = new ArrayList<>(tours.getAcceptedTourIds());
                   for (String pendingTour : newToursData.getPendingTourIds()){
                       if (!currentPending.contains(pendingTour) && !acceptedTours.contains(pendingTour)){
                           pendingToursToAdd.add(pendingTour);
                           currentPending.add(pendingTour);
                       }
                   }
                    tours.setPendingTourIds(currentPending);
                    if (pendingToursToAdd.size() > 0) {
                        db.collection("tours").whereIn(FieldPath.documentId(), pendingToursToAdd).get().addOnSuccessListener(
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

                } else {
                    Log.d("TourDocListener", "Current data: null");
                }
            }
        });
    }

}



