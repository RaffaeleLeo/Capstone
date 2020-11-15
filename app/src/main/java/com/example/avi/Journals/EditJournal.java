package com.example.avi.Journals;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.example.avi.MapsActivity;
import com.example.avi.MyDBHandler;
import com.example.avi.Notifications;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.avi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class EditJournal extends AppCompatActivity {

    private String journal_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journal);
        final MyDBHandler dbHandler = new MyDBHandler(getApplicationContext(), "journals.db", null, 1);
        final Intent intent = getIntent();


        setupUI(intent);

        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.journal_type);
        String[] items = new String[]{"Skiing", "Snowboarding", "Hiking", "Peak"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

        Button save_button = findViewById(R.id.saveButton);

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog("save", dbHandler);

            }
        });

        Button delete_button = findViewById(R.id.deleteButton);

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createDialog("delete", dbHandler);


            }
        });

        Button view_on_map = findViewById(R.id.view_on_map);

        view_on_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPathOnMap(intent);

            }
        });

        Button upload = findViewById(R.id.upload_journal);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload_journal(intent);

            }
        });

    }

    /***
     * uploads the current journal to firebase
     * @param Intent
     */
    private void upload_journal(Intent Intent)
    {
        //gets the unique firebase user ID
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Journal Journal = (Journal) Intent.getSerializableExtra("journal");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(currentUser + "/journals");

        //get the users latest edits
        EditText jName = (EditText)findViewById(R.id.journalName);
        Journal.name = jName.getText().toString();

        EditText jDesk = (EditText)findViewById(R.id.journal_desc_content);
        Journal.description = jDesk.getText().toString();

        if (Journal != null) {
            //Save the journal data to the database//
            ref.child(Journal.name).setValue(Journal);
        }

        //if the user had coordinates from tracking, make sure to get those too
        final MyDBHandler dbHandler_location = new MyDBHandler(getApplicationContext(),
                "data_points.db", null, 1);
        List<Double> coords = dbHandler_location.getAllData(Intent.getStringExtra("Name"));

        //push to firebase
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference(
                currentUser + "/journals/" + Journal.name + "/coordinates");
        ref2.setValue(coords);

        Toast.makeText(EditJournal.this, "Successfully uploaded to cloud", Toast.LENGTH_LONG).show();
    }

    /***
     * show the path of the current journal on the map
     * @param intent
     */
    private void showPathOnMap(Intent intent)
    {
        Intent intent_2 = new Intent(EditJournal.this, MapsActivity.class);
        intent_2.putExtra("journal_name", intent.getStringExtra("Name"));
        startActivity(intent_2);
    }

    /***
     * gets called when the save or delete buttons are pressed
     *
     * If the save button is pressed, it saves the changes in the local MYSQL database and returns
     * to the journals page
     *
     * If the delete button is pressed, it deletes the current journal from the local database
     * and the remote database if it was uploaded previously
     * @param action
     * @param dbHandler
     */
    private void createDialog(String action, final MyDBHandler dbHandler) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);


        if(action.equals("delete")) {
            alertDialog.setMessage("Are You Sure You Want To Delete This Journal?");
            alertDialog.setTitle("Delete Journal");

            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                Log.d(TAG, "onClick: Yes delete the Goal");
                    System.out.println("onClick: Yes delete the Journal");

                    //removes the object from firebase
                    String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                            currentUser + "/journals").child(journal_name);
                    ref.removeValue();

                    //removes the object from the local database
                    dbHandler.deleteFromJournals(getIntent().getStringExtra("Name"));

                    //tell the user the journal was deleted and go back to the journals screen
                    Toast.makeText(EditJournal.this, "The Journal Has Been Deleted", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EditJournal.this, JournalActivity.class);
                    startActivity(intent);
                }
            });

            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.out.println("onClick: No do not delete the Journal");
                }
            });
        }else{

            alertDialog.setMessage("Save Changes to Current Journal?");
            alertDialog.setTitle("Save Changes");

            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.out.println("onClick: Yes save changes to the Journal");

                    ToggleButton remindersToggle = findViewById(R.id.journal_tracking);

                    //collect all the edits the user made
                    EditText jName = (EditText)findViewById(R.id.journalName);
                    String journal_name = jName.getText().toString();

                    EditText jDesk = (EditText)findViewById(R.id.journal_desc_content);
                    String journal_description = jDesk.getText().toString();

                    Spinner jType = findViewById(R.id.journal_type);
                    String journal_type = jType.getSelectedItem().toString();

                    //save those edits in the local database
                    dbHandler.editJournal(getIntent().getStringExtra("Name"), journal_name, journal_description, remindersToggle.isChecked(), journal_type);
                    if(remindersToggle.isChecked()) {
                        Notifications notifier = new Notifications();
                        notifier.notification("Route tracking started.", "Your location will be periodically recorded.", (int) System.currentTimeMillis(), getApplicationContext());
                    }
                    Intent intent = new Intent(EditJournal.this, JournalActivity.class);
                    startActivity(intent);
                }
            });

            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {



                    System.out.println("onClick: No do not save the changes to the Goal");
                }
            });
        }
        alertDialog.show();

    }

    /***
     * sets up the UI with the selected journals name, description, type, and if it's currently
     * recording or not
     * @param intent
     */
    private void setupUI(Intent intent) {

        TextView journal_name = findViewById(R.id.journalName);

        this.journal_name = intent.getStringExtra("Name");

        journal_name.setText(this.journal_name);

        TextView journal_desc = findViewById(R.id.journal_desc_content);

        journal_desc.setText(intent.getStringExtra("Description"));


        ToggleButton remindersToggle = findViewById(R.id.journal_tracking);
        remindersToggle.setChecked(intent.getBooleanExtra("Tracking", false));
    }


}
