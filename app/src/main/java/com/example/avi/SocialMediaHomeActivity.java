package com.example.avi;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.avi.ChatRoom.ChatRoomActivity;
import com.example.avi.ChatRoom.User;
import com.example.avi.Journals.Journal;
import com.example.avi.Journals.JournalActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

public class SocialMediaHomeActivity extends AppCompatActivity {

    Button settings;
    Button chat;
    Button friends;
    Boolean behaveNormallyWhenBackIsPressed;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    public User user;

    private Context context;

    private LinearLayout toursLinearLayout;
    private ImageButton addTourButton;
    private ConstraintLayout rootLayout;
    private ArrayList<String> userFriendsEmails;
    TextView topBarText;

    @Override
    protected void onStart(){
        super.onStart();

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("AreInTours", true);
        editor.commit();
    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("AreInTours",  false);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TabLayout tabLayout = findViewById(R.id.TabLayout);
        tabLayout.getTabAt(3).select();
    }

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        behaveNormallyWhenBackIsPressed = true;
        setContentView(R.layout.activity_social_media_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rootLayout = findViewById(R.id.social_media_constraint);
        settings = findViewById(R.id.topBar).findViewById(R.id.settingsButton);
        TextView title = findViewById(R.id.topBar).findViewById(R.id.pageTitle);
        title.setText("Social");
        chat = findViewById(R.id.gotoChat);
        friends = findViewById(R.id.gotoFriends);
        toursLinearLayout = findViewById(R.id.tour_linear_layout);
        addTourButton = findViewById(R.id.add_tour);
        userFriendsEmails = new ArrayList<>();
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
                if (user != null) {
                    Log.d("user", user.getId());
                }
                setUpModifiedTourListeners();
                getFriendsEmails();

            }
        });
    }

    private void setupAddTour() {
        addTourButton.setOnClickListener(new View.OnClickListener() {
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
                final EditText latLonText = newTour.findViewById(R.id.coordinates_text);
                final TextView dateWarning = newTour.findViewById(R.id.date_required_label);
                final TextView timeWarning = newTour.findViewById(R.id.time_required_label);
                Button journalCoordButton = newTour.findViewById(R.id.journal_coordinates);
                Button friendsButton = newTour.findViewById(R.id.gotoFriends);

                setUpDateListeners(newDate);
                setUpTimeListeners(newTourTime);
                setUpFriendsButtonDialogue(friendsButton, newInvitedText);
                setUpJournalCoordinatesClickListener(journalCoordButton, latLonText);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        behaveNormallyWhenBackIsPressed = true;
                        final String tourText;
                        final String dateText;
                        final String timeText;
                        String notesText;
                        String invitedText;
                        String coordinates;


                        tourText = newTourName.getText().toString().trim();
                        dateText = newDate.getText().toString().trim();
                        timeText = newTourTime.getText().toString().trim();
                        notesText = newTourNotes.getText().toString().trim();
                        invitedText = newInvitedText.getText().toString().trim();
                        coordinates = latLonText.getText().toString().trim();


                        if (dateText.isEmpty() && timeText.isEmpty()) {
                            dateWarning.setVisibility(View.VISIBLE);
                            timeWarning.setVisibility(View.VISIBLE);

                        } else if (dateText.isEmpty() && !timeText.isEmpty()) {
                            dateWarning.setVisibility(View.VISIBLE);
                            timeWarning.setVisibility(View.GONE);
                        } else if (!dateText.isEmpty() && timeText.isEmpty()) {
                            dateWarning.setVisibility(View.GONE);
                            timeWarning.setVisibility(View.VISIBLE);
                        } else {

                            ArrayList<String> tourOwners = new ArrayList<>();
                            ArrayList<String> acceptedList = new ArrayList<>();
                            ArrayList<String> pendingList = new ArrayList<>(Arrays.asList(invitedText.split("\n")));
                            removeSpaceFromStringArray(pendingList);
                            tourOwners.add(user.getEmail());
                            acceptedList.add(user.getEmail());
                            final Tours.Tour tour = new Tours.Tour(tourText, tourOwners, dateText, timeText, notesText, acceptedList, pendingList, coordinates);
                            db.collection("tours").add(tour).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    int howManyHoursEarly = Integer.parseInt(getApplicationContext().getSharedPreferences("Prefs", 0).getString("notifyHours", "1"));
                                    Calendar c = new GregorianCalendar();
                                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyyHH:mm", Locale.ENGLISH);
                                    try {
                                        c.setTime(sdf.parse(dateText+timeText));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    c.add(Calendar.HOUR, -howManyHoursEarly);

                                    Intent alarmIntent = new Intent(context, AlarmNotification.class);
                                    alarmIntent.putExtra("title", "Upcoming tour");
                                    alarmIntent.putExtra("text", tourText + " in " + howManyHoursEarly + " hour(s).");
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) c.getTimeInMillis(), alarmIntent, 0);
                                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                    alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

                                    //Notifications notifier = new Notifications();
                                    //notifier.notification(dateText, timeText,(int) System.currentTimeMillis(), context);
                                    Log.d("newTour", "Successfully added new Tour");

                                }

                            });
                            rootLayout.removeView(newTour);
                        }
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

    private void removeSpaceFromStringArray(ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i).trim());
            if (list.get(i).length() == 0) {
                list.remove(i);
            }
        }

    }

    private void setUpEditTourInfoButton(ImageButton editButton) {
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
                final EditText newTourCoord = newTour.findViewById(R.id.coordinates_text);
                Button friendsButton = newTour.findViewById(R.id.gotoFriends);
                Button journalCoordButton = newTour.findViewById(R.id.journal_coordinates);

                setUpDateListeners(newDate);
                setUpTimeListeners(newTourTime);
                setUpFriendsButtonDialogue(friendsButton, newInvitedText);
                setUpJournalCoordinatesClickListener(journalCoordButton, newTourCoord);

                final TextView tourName = tourBox.findViewById(R.id.tour_name);
                final TextView tourDate = tourBox.findViewById(R.id.date_text);
                final TextView tourTime = tourBox.findViewById(R.id.time_text);
                final TextView tourNotes = tourBox.findViewById(R.id.notes_text);
                final TextView tourInvites = tourBox.findViewById(R.id.invites_text);
                final TextView tourCoord = tourBox.findViewById(R.id.coordinates_text);
                newTourName.setText(tourName.getText());
                newDate.setText(tourDate.getText());
                newTourTime.setText(tourTime.getText());
                newTourNotes.setText(tourNotes.getText());
                newInvitedText.setText(tourInvites.getText());
                newTourCoord.setText(tourCoord.getText());
                rootLayout.addView(newTour);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        behaveNormallyWhenBackIsPressed = true;
                        final String tourText;
                        final String dateText;
                        final String timeText;
                        String notesText;
                        String invitedText;
                        final String coordinates;

                        tourText = newTourName.getText().toString().trim();
                        dateText = newDate.getText().toString().trim();
                        timeText = newTourTime.getText().toString().trim();
                        notesText = newTourNotes.getText().toString().trim();
                        invitedText = newInvitedText.getText().toString().trim();
                        coordinates = newTourCoord.getText().toString().trim();


                        tourInvites.setVisibility(View.GONE);

                        tourName.setText(tourText);
                        tourDate.setText(dateText);
                        tourTime.setText(timeText);
                        tourNotes.setText(notesText);
                        tourInvites.setText(invitedText);
                        tourCoord.setText(coordinates);


                        //Data base changes
                        final String finalTourText = tourText;
                        final ArrayList<String> invitees = new ArrayList<>(Arrays.asList(invitedText.split("\n")));
                        removeSpaceFromStringArray(invitees);
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
                                for (String person : tourEdit.acceptedInvitees) {
                                    if (!invitees.contains(person) && !person.equals(user.getEmail())) {
                                        removeInvitees.add(person);
                                        removeAccepted.add(person);
                                    } else {
                                        addPending.remove(person);
                                    }
                                }
                                tourEdit.acceptedInvitees.removeAll(removeAccepted);
                                for (String person : tourEdit.pendingInvitees) {
                                    if (!invitees.contains(person)) {
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
                                tourEdit.latLon = coordinates;

                                db.collection("tours").document(tourId).set(tourEdit).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        int howManyHoursEarly = Integer.parseInt(getApplicationContext().getSharedPreferences("Prefs", 0).getString("notifyHours", "1"));//getApplicationContext().getSharedPreferences("Prefs", 0).getInt("notifyHours", 1);
                                        Calendar c = new GregorianCalendar();
                                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyyHH:mm", Locale.ENGLISH);
                                        try {
                                            c.setTime(sdf.parse(dateText+timeText));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        c.add(Calendar.HOUR, -howManyHoursEarly);

                                        Intent alarmIntent = new Intent(context, AlarmNotification.class);
                                        alarmIntent.putExtra("title", "Upcoming tour");
                                        alarmIntent.putExtra("text", tourText + " in " + howManyHoursEarly + " hour(s).");
                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) c.getTimeInMillis(), alarmIntent, 0);
                                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("TourFailure", "Edit Tour Failed in database");
                                    }
                                });
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

    private void setupToursView(String type, ArrayList<Tours.Tour> tours, ArrayList<String> tourIds) throws ParseException {
        switch (type) {
            case "Pending":
                for (int i = 0; i < tours.size(); i++) {
                    final View pendingTourBox = getLayoutInflater().inflate(R.layout.pending_tour_box, toursLinearLayout, false);
                    final TextView tourName = pendingTourBox.findViewById(R.id.tour_name);
                    final TextView tourDate = pendingTourBox.findViewById(R.id.date_text);
                    final TextView tourTime = pendingTourBox.findViewById(R.id.time_text);
                    TextView tourNotes = pendingTourBox.findViewById(R.id.notes_text);
                    TextView tourCoordinates = pendingTourBox.findViewById(R.id.coordinates_text);
                    final TextView tourInvites = pendingTourBox.findViewById(R.id.invites_text);
                    ImageButton invitesButton = pendingTourBox.findViewById(R.id.invites_button);
                    ImageButton acceptButton = pendingTourBox.findViewById(R.id.acceptButton);
                    ImageButton declineButton = pendingTourBox.findViewById(R.id.declineButton);


                    tourInvites.setVisibility(View.GONE);

                    setupTourInvitesButton(invitesButton, tourInvites);
                    setUpCoordinatesClickListener(tourCoordinates);
                    Tours.Tour tour = tours.get(i);
                    pendingTourBox.setTag(tourIds.get(i));
                    acceptButton.setTag(pendingTourBox);
                    declineButton.setTag(pendingTourBox);
                    tourName.setTag("Pending");
                    acceptButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            View pendingTourBox = (View) view.getTag();
                            String tourId = (String) pendingTourBox.getTag();
                            db.collection("tours").document(tourId).update("acceptedInvitees", FieldValue.arrayUnion(user.getEmail()));
                            db.collection("tours").document(tourId).update("pendingInvitees", FieldValue.arrayRemove(user.getEmail()));
                            int howManyHoursEarly = Integer.parseInt(getApplicationContext().getSharedPreferences("Prefs", 0).getString("notifyHours", "1"));//getApplicationContext().getSharedPreferences("Prefs", 0).getInt("notifyHours", 1);
                            Calendar c = new GregorianCalendar();
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyyHH:mm", Locale.ENGLISH);
                            try {
                                c.setTime(sdf.parse(tourDate.getText().toString() + tourTime.getText().toString()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            c.add(Calendar.HOUR, -howManyHoursEarly);

                            Intent alarmIntent = new Intent(context, AlarmNotification.class);
                            alarmIntent.putExtra("title", "Upcoming tour");
                            alarmIntent.putExtra("text", tourName.getText().toString() + " in " + howManyHoursEarly + " hour(s).");
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) c.getTimeInMillis(), alarmIntent, 0);
                            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                        }
                    });

                    declineButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            View pendingTourBox = (View) view.getTag();
                            String tourId = (String) pendingTourBox.getTag();
                            db.collection("tours").document(tourId).update("pendingInvitees", FieldValue.arrayRemove(user.getEmail()));
                        }
                    });
                    tourName.setText(tour.tourName);
                    tourDate.setText(tour.date);
                    tourTime.setText(tour.time);
                    tourNotes.setText(tour.notes);
                    tourCoordinates.setText(tour.latLon);
                    StringBuilder sb = new StringBuilder();
                    for (String person : tour.acceptedInvitees) {
                        sb.append(person + "\n");
                    }
                    for (String person : tour.pendingInvitees) {
                        if (!person.equals(user.getEmail())) {
                            sb.append(person + "\n");
                        }
                    }
                    tourInvites.setText(sb.toString().trim());
                    toursLinearLayout.addView(pendingTourBox, getTourInsertPosition(pendingTourBox));
                }
                break;
            case "Accepted":

                for (int i = 0; i < tours.size(); i++) {
                    final ArrayList<String> owners = new ArrayList<>(tours.get(i).tourOwners);
                    View acceptedTourBox = getLayoutInflater().inflate(R.layout.tour_box, toursLinearLayout, false);
                    acceptedTourBox.setTag(tourIds.get(i));
                    final ImageButton editTourButton = acceptedTourBox.findViewById(R.id.edit_tour_button);
                    final ImageButton deleteButton = acceptedTourBox.findViewById(R.id.delete_button);
                    deleteButton.setTag(acceptedTourBox);
                    editTourButton.setTag(acceptedTourBox);
                    final ImageButton editButton = acceptedTourBox.findViewById(R.id.edit_button);
                    final Button findMembersButton = acceptedTourBox.findViewById(R.id.tours_tracking_button);
                    editButton.setTag(acceptedTourBox);

                    if (owners.contains(user.getEmail())) {
                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                View tourBox = (View) view.getTag();
                                final String tourId = (String) tourBox.getTag();
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
                    TextView tourCoordinates = acceptedTourBox.findViewById(R.id.coordinates_text);

                    final TextView tourInvites = acceptedTourBox.findViewById(R.id.invites_text);
                    ImageButton invitesButton = acceptedTourBox.findViewById(R.id.invites_button);

                    tourInvites.setVisibility(View.GONE);

                    setupTourInvitesButton(invitesButton, tourInvites);
                    setUpCoordinatesClickListener(tourCoordinates);
                    Tours.Tour tour = tours.get(i);
                    tourName.setText(tour.tourName);
                    tourDate.setText(tour.date);
                    tourTime.setText(tour.time);
                    tourNotes.setText(tour.notes);
                    tourName.setTag("Accepted");
                    tourCoordinates.setText(tour.latLon);
                    StringBuilder sb = new StringBuilder();
                    for (String person : tour.acceptedInvitees) {
                        if (!person.equals(user.getEmail())) {
                            sb.append(person + "\n");
                        }
                    }
                    for (String person : tour.pendingInvitees) {
                        if (!person.equals(user.getEmail())) {
                            sb.append(person + "\n");
                        }
                    }
                    tourInvites.setText(sb.toString().trim());
                    setUpTourTrackingButton(findMembersButton, tour.date + " " + tour.time, tourIds.get(i));
                    toursLinearLayout.addView(acceptedTourBox, getTourInsertPosition(acceptedTourBox));
                }
                break;
        }

    }

    private void setUpTourTrackingButton(Button toursTrackingButton, String dateString, final String tourId) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
        String formattedDate = dateFormat.format(calendar.getTime());
        Date now = dateFormat.parse(formattedDate);
        Date date = new SimpleDateFormat("MM/dd/yyyy HH:mm").parse(dateString);
       if (DateUtils.isToday(date.getTime()) && date.before(now)){
           toursTrackingButton.setVisibility(View.VISIBLE);
           toursTrackingButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent(SocialMediaHomeActivity.this, MapsActivity.class);
                   intent.putExtra("tourId", tourId);
                   startActivity(intent);
               }
           });
       }

    }


    private void setUpJournalCoordinatesClickListener(Button journalButton, final EditText coordinateText){
        journalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MyDBHandler dbHandler = new MyDBHandler(getApplicationContext(),
                        "journals.db", null, 1);
                final MyDBHandler dbHandler_location = new MyDBHandler(getApplicationContext(),
                        "data_points.db", null, 1);
                ArrayList<Journal> journals = new ArrayList<Journal>();
                journals = dbHandler.getAllJournals();
                ArrayList<String> journalNames = new ArrayList<>();
                final ArrayList<String> startingCoord = new ArrayList<>();
                for (int i = 0; i < journals.size(); i++){
                    ArrayList<Double> dataPoints = dbHandler_location.getAllData(journals.get(i).name);
                    if (dataPoints.size() > 0) {
                        journalNames.add(journals.get(i).name);
                        startingCoord.add(dataPoints.get(0).toString() + ", " + dataPoints.get(1).toString());
                    }
                }
                dbHandler.close();
                dbHandler_location.close();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Get coordinates from journal");
                String[] names = journalNames.toArray(new String[0]);
                final String coordinates = coordinateText.getText().toString();
                int index = 0;
                if (coordinates != null && !coordinates.trim().isEmpty()){
                    for (int i = 0; i < startingCoord.size(); i++){
                        if (coordinates.equals(startingCoord.get(i))){
                            index = i;
                        }
                    }
                }
                final int checkedItem = index;
                if (index != 0){
                    coordinateText.setText(startingCoord.get(index));
                }else if (startingCoord.size() > 0){
                    coordinateText.setText(startingCoord.get(0));
                }
                builder.setSingleChoiceItems(names, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        coordinateText.setText(startingCoord.get(which));
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        coordinateText.setText(coordinates);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

    }

    private void setUpCoordinatesClickListener(final TextView coordinatesText) {
        coordinatesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://maps.google.com/maps?saddr=" + "&daddr=" + coordinatesText.getText().toString();
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });
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


    public int getTourInsertPosition(View newView) throws ParseException {
        int numChildren = toursLinearLayout.getChildCount();
        if (numChildren == 1) {
            return 1;
        }
        Date now;
        String tourStatus = (String) newView.findViewById(R.id.tour_name).getTag();
        TextView dateText = newView.findViewById(R.id.date_text);
        TextView timeText = newView.findViewById(R.id.time_text);
        Date date = new SimpleDateFormat("MM/dd/yyyy HH:mm").parse(dateText.getText().toString() + " " + timeText.getText().toString());
        Calendar calendar = Calendar.getInstance();
        try {
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
            String formattedDate = dateFormat.format(calendar.getTime());
            now = dateFormat.parse(formattedDate);
            Log.d("now", now.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (int i = 1; i < numChildren; i++) {
            View child = toursLinearLayout.getChildAt(i);
            String childTourStatus = (String) child.findViewById(R.id.tour_name).getTag();
            TextView childDateText = child.findViewById(R.id.date_text);
            TextView childTimeText = child.findViewById(R.id.time_text);
            Date childDate = new SimpleDateFormat("MM/dd/yyyy HH:mm").parse(childDateText.getText().toString() + " " + childTimeText.getText().toString());

            if (!tourStatus.equals(childTourStatus) && childTourStatus.equals("Accepted")) {
                return i;
            } else if (tourStatus.equals(childTourStatus)) {
                if (childDate.after(date)) {
                    return i;
                }
            }
        }
        return numChildren;

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

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yyyy";
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

    public void setUpTimeListeners(final TextInputEditText editTime) {
        editTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(SocialMediaHomeActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String hour = String.valueOf(selectedHour);
                        String min = String.valueOf(selectedMinute);
                        if (hour.length() < 2) {
                            hour = "0" + hour;
                        }
                        if (min.length() < 2) {
                            min = "0" + min;
                        }
                        editTime.setText(hour + ":" + min);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });
    }

    public void setUpModifiedTourListeners() {

        db.collection("tours")
                .whereArrayContains("acceptedInvitees", user.getEmail())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("tourListenFail", "listen:error", e);
                            return;
                        }
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    ArrayList<Tours.Tour> acceptedUserTour = new ArrayList<>();
                                    acceptedUserTour.add(dc.getDocument().toObject(Tours.Tour.class));
                                    ArrayList<String> tourIds = new ArrayList<>();
                                    tourIds.add(dc.getDocument().getId());
                                    Log.d("user", acceptedUserTour.get(0).toString());
                                    try {
                                        setupToursView("Accepted", acceptedUserTour, tourIds);
                                    } catch (ParseException ex) {
                                        ex.printStackTrace();
                                    }
                                    break;
                                case MODIFIED:
                                    Log.d("tourMod", "Modified tour: " + dc.getDocument().getData());
                                    Tours.Tour modTour = dc.getDocument().toObject(Tours.Tour.class);
                                    toursLinearLayout.removeView(toursLinearLayout.findViewWithTag(dc.getDocument().getId()));
                                    ArrayList<Tours.Tour> modUserTours = new ArrayList<>();
                                    modUserTours.add(modTour);
                                    ArrayList<String> modTourIds = new ArrayList<>();
                                    modTourIds.add(dc.getDocument().getId());
                                    try {
                                        setupToursView("Accepted", modUserTours, modTourIds);
                                    } catch (ParseException ex) {
                                        ex.printStackTrace();
                                    }

                                    break;
                                case REMOVED:
                                    Log.d("tourDelete", "Removed tour: " + dc.getDocument().getData());
                                    toursLinearLayout.removeView(toursLinearLayout.findViewWithTag(dc.getDocument().getId()));
                                    break;
                            }
                        }
                    }
                });
        db.collection("tours")
                .whereArrayContains("pendingInvitees", user.getEmail())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("tourListenFail", "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    ArrayList<Tours.Tour> pendingUserTours = new ArrayList<>();
                                    pendingUserTours.add(dc.getDocument().toObject(Tours.Tour.class));
                                    ArrayList<String> tourIds = new ArrayList<>();
                                    tourIds.add(dc.getDocument().getId());
                                    Log.d("user", pendingUserTours.get(0).toString());
                                    try {
                                        setupToursView("Pending", pendingUserTours, tourIds);
                                    } catch (ParseException ex) {
                                        ex.printStackTrace();
                                    }
                                    break;
                                case MODIFIED:
                                    Log.d("tourMod", "Modified Tour: " + dc.getDocument().getData());
                                    Tours.Tour modTour = dc.getDocument().toObject(Tours.Tour.class);
                                    toursLinearLayout.removeView(toursLinearLayout.findViewWithTag(dc.getDocument().getId()));
                                    ArrayList<Tours.Tour> modUserTours = new ArrayList<>();
                                    modUserTours.add(modTour);
                                    ArrayList<String> modTourIds = new ArrayList<>();
                                    modTourIds.add(dc.getDocument().getId());
                                    try {
                                        setupToursView("Pending", modUserTours, modTourIds);
                                    } catch (ParseException ex) {
                                        ex.printStackTrace();
                                    }
                                    break;
                                case REMOVED:
                                    Log.d("tourDelete", "Remove tour: " + dc.getDocument().getData());
                                    toursLinearLayout.removeView(toursLinearLayout.findViewWithTag(dc.getDocument().getId()));
                                    break;
                            }
                        }

                    }
                });
    }

    private void getFriendsEmails() {
        ArrayList<String> userFriends = new ArrayList<>(user.getFriends().keySet());
        if (userFriends.size() > 0) {
            db.collection("users").whereIn("id", userFriends).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (DocumentSnapshot docRef : queryDocumentSnapshots.getDocuments()) {
                        userFriendsEmails.add(docRef.get("email").toString());
                        Log.d("friendsList", docRef.get("email").toString());
                    }
                }
            });
        }
    }

    private void setUpFriendsButtonDialogue(Button friendsButton, final TextInputEditText invitees) {
        final boolean[] checkedFriends = new boolean[userFriendsEmails.size()];
        String[] friends = new String[userFriendsEmails.size()];
        friends = userFriendsEmails.toArray(friends);
        final String[] finalFriends = friends;
        final ArrayList<Integer> userFriends = new ArrayList<>();
        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String> invites = new ArrayList<>(Arrays.asList(invitees.getText().toString().split("\n")));
                removeSpaceFromStringArray(invites);
                final ArrayList<String> originInvites = new ArrayList<>(invites);
                Log.d("friendsList", "Friends Button Clicked");
                Log.d("friendsList", invites.toString());
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SocialMediaHomeActivity.this);
                mBuilder.setTitle("Select Friends to Invite");
                for (int i = 0; i < checkedFriends.length; i++) {
                    if (invites.contains(finalFriends[i])) {
                        checkedFriends[i] = true;
                    }
                }
                mBuilder.setMultiChoiceItems(finalFriends, checkedFriends, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                    }
                });
                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < checkedFriends.length; i++) {
                            if (checkedFriends[i]) {
                                if (!invites.contains(finalFriends[i])) {
                                    invites.add(finalFriends[i]);
                                }
                            } else {
                                if (invites.contains(finalFriends[i])) {
                                    invites.remove(finalFriends[i]);
                                }
                            }
                        }
                        for (String friend : invites) {
                            sb.append(friend + "\n");
                        }
                        invitees.setText(sb.toString().trim());
                    }
                });
                mBuilder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder sb = new StringBuilder();
                        for (String friend : originInvites) {
                            sb.append(friend + "\n");
                        }
                        invitees.setText(sb.toString().trim());
                    }
                });
                mBuilder.show();
            }
        });
    }

}



