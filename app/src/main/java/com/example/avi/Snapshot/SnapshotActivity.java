package com.example.avi.Snapshot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.avi.ChatRoom.ChatRoomActivity;
import com.example.avi.ChatRoom.Message;
import com.example.avi.Journals.JournalActivity;
import com.example.avi.LiveUpdates;
import com.example.avi.MapsActivity;
import com.example.avi.R;
import com.example.avi.SocialMediaHomeActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SnapshotActivity extends AppCompatActivity{

    private float elevation;
    private float aspect;

    TextView elevText;
    TextView aspectText;
    Button sendButton;
    Button gotoChatButton;

    private String currSelection;
    private String currFinalMessage;

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mAuth;


    @Override
    protected void onResume(){
        super.onResume();
        TabLayout tabLayout = findViewById(R.id.TabLayout);
        tabLayout.getTabAt(getIntent().getIntExtra("PRIOR", 0)).select();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapshot);
        setupTabLayout();

        //Get elevation and aspect from map activity
        elevation = getIntent().getFloatExtra("elevation", 0f);
        aspect = getIntent().getFloatExtra("aspect", 0f);

        //Set them in the header textview
        elevText = findViewById(R.id.Altitude);
        aspectText =  findViewById(R.id.Heading);
        elevText.setText(Float.toString(elevation) + " FT");
        aspectText.setText(Float.toString(aspect) + "°");

        //Set content of message spinner
        Spinner mspinner = findViewById(R.id.SelectMessage);
        ArrayList<String> messageStrings = new ArrayList<>();
        messageStrings.add("Select a message:");
        messageStrings.add("Great snow!");
        messageStrings.add("Recent avalanche activity.");
        messageStrings.add("Observed snow instability.");
        messageStrings.add("Wind loading.");
        messageStrings.add("Low snowpack.");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, messageStrings);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mspinner.setAdapter(dataAdapter);

        mspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                currSelection = parentView.getItemAtPosition(position).toString();
                if(currSelection!=null){
                    if(!currSelection.equals("Select a message:")) {
                        currFinalMessage = currSelection + "\n";
                        currFinalMessage = currFinalMessage + "Elevation: " + elevation + "\n";
                        currFinalMessage = currFinalMessage + "Aspect: " + aspect + "\n";
                        Date currentTime = Calendar.getInstance().getTime();
                        String temp = currentTime.toString();
                        currFinalMessage = currFinalMessage + "Date/Time: " + temp;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        //Set up chat database connection / send functionality
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        final DatabaseReference ref = database.getReference("messages");
        sendButton = findViewById(R.id.SendMessage);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currFinalMessage != null) {
                    final Message msg = new Message(currFinalMessage, mAuth.getUid(),"n/a", true);
                    ref.push().setValue(msg);
                    currFinalMessage = null;
                    currSelection = null;
                    Toast.makeText(getApplicationContext(),"Message Sent!", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Set up button to view chat
        gotoChatButton = findViewById(R.id.jumpToChat);
        gotoChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SnapshotActivity.this, ChatRoomActivity.class);
                startActivity(intent);
            }
        });



    }



    /**
     * sets up the tab layout at the bottom of the screen
     */
    private void setupTabLayout() {
        TabLayout tabLayout = findViewById(R.id.TabLayout);
        tabLayout.getTabAt(getIntent().getIntExtra("PRIOR", 0)).select();
        TabLayout.OnTabSelectedListener listener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 2) {
                    Intent intent = new Intent(SnapshotActivity.this, JournalActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 3) {
                    Intent intent = new Intent(SnapshotActivity.this, SocialMediaHomeActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 0) {
                    Intent intent = new Intent(SnapshotActivity.this, LiveUpdates.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 1) {
                    Intent intent = new Intent(SnapshotActivity.this, MapsActivity.class);

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


        tabLayout.addOnTabSelectedListener(listener);


    }

}

