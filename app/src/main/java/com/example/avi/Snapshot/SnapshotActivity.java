package com.example.avi.Snapshot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.avi.ChatRoom.ChatRoomActivity;
import com.example.avi.ChatRoom.Message;
import com.example.avi.Journals.JournalActivity;
import com.example.avi.LiveUpdates;
import com.example.avi.MapsActivity;
import com.example.avi.MyDBHandler;
import com.example.avi.R;
import com.example.avi.SocialMediaHomeActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class SnapshotActivity extends AppCompatActivity{

    private float elevation;
    private float aspect;

    TextView elevText;
    TextView aspectText;
    Button sendButton;
    Button gotoChatButton;
    Button saveSnapshot;
    EditText snapshotName;

    private String currSelection;
    private String currFinalMessage;

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mAuth;

    private MyDBHandler dbHandler;

    private ArrayList<Snapshot> snapshotList = new ArrayList<Snapshot>();

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
        elevText.setText(elevation + " FT");
        aspectText.setText(aspect + "°");

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

        //Populate list with all current snapshots
        ListView slist = (ListView) findViewById(R.id.snapshot_list);
        final listviewAdapter adapter = new listviewAdapter(this, snapshotList);
        slist.setAdapter(adapter);
        dbHandler = new MyDBHandler(getApplicationContext(), "snapshot.db", null, 1);
        ArrayList<Snapshot> tempList = dbHandler.getAllSnapshots();
        snapshotList.clear();
        snapshotList.addAll(tempList);
        slist.setSelection(slist.getCount() - 1);
        adapter.notifyDataSetChanged();


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

        //Set up and populate snapshot list
        saveSnapshot = findViewById(R.id.SaveSnapshot);
        snapshotName = findViewById(R.id.NameSnapshot);
        snapshotName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(snapshotName.getText().toString().equals("Enter name")) {
                    snapshotName.setText("");
                }
            }
        });

        //Button to save snapshot in database
        saveSnapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(snapshotName.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"Please give snapshot a name", Toast.LENGTH_LONG).show();
                }
                else{
                    String name = snapshotName.getText().toString();
                    Date currentTime = Calendar.getInstance().getTime();
                    String date = currentTime.toString();
                    int loc = getCompassLocation(elevation, aspect);
                    int danger = dbHandler.getDangerAtLocation(loc);
                    String dangerString = danger + ": " + dangerDesc.get(danger);
                    Snapshot s = new Snapshot(name, elevation + "", aspect + "°", dangerString, date);
                    dbHandler.addToSnapshot(s.getName(), s.getElevation(), s.getAspect(), s.getRating(), s.getDate());
                    snapshotList.add(s);
                    adapter.notifyDataSetChanged();
                    snapshotName.setText("");
                }
            }
        });

        slist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position,
                                    long id)
            {
//                Snapshot s = (Snapshot) parent.getItemAtPosition(position);
//                String date = s.getDate();
//                dbHandler.deleteFromSnapshot(date);
//                snapshotList.remove(position);
//                adapter.notifyDataSetChanged();

                AlertDialog.Builder builder = new AlertDialog.Builder(SnapshotActivity.this);
                builder.setMessage("Delete Snapshot?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Snapshot s = (Snapshot) parent.getItemAtPosition(position);
                        String date = s.getDate();
                        dbHandler.deleteFromSnapshot(date);
                        snapshotList.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
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

    private int getCompassLocation(float elevation, float degrees){
        int res = 0;
        if(elevation < 5000){
            return -1;
        }
        else if(elevation <= 7000){
            res = 16;
        }
        else if(elevation <= 8500){
            res = 8;
        }
        else{
            res = 0;
        }

        if(degrees >= 22.5 && degrees < 67.5){
            res = res + 1;
        }

        else if(degrees >= 67.5 && degrees < 112.5){
            res = res + 2;
        }

        else if(degrees >= 112.5 && degrees < 157.5){
            res = res + 3;
        }

        else if(degrees >= 157.5 && degrees < 202.5){
            res = res + 4;
        }

        else if(degrees >= 202.5 && degrees < 247.5){
            res = res + 5;
        }

        else if(degrees >= 247.5 && degrees < 292.5){
            res = res + 6;
        }

        else if(degrees >= 292.5 && degrees < 337.5){
            res = res + 7;
        }
        else{
            res = res + 0;
        }


        return res;
    }

}

//public class DeleteSnapshotDialogFragment extends DialogFragment {
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        // Use the Builder class for convenient dialog construction
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setMessage("Delete Snapshot?")
//                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User cancelled the dialog
//                    }
//                });
//        return builder.create();
//    }
//}

