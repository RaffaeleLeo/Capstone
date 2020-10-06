package com.example.avi.Journals;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.avi.LoginActivity;
import com.example.avi.MapsActivity;
import com.example.avi.MyDBHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.avi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class EditJournal extends AppCompatActivity {

    private String journal_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journal);
        final MyDBHandler dbHandler = new MyDBHandler(getApplicationContext(), "journals.db", null, 1);
        final Intent intent = getIntent();


        setupUI(intent);

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

    }

    private void showPathOnMap(Intent intent)
    {
        Intent intent_2 = new Intent(EditJournal.this, MapsActivity.class);

        //intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
        intent_2.putExtra("journal_name", intent.getStringExtra("Name"));
        startActivity(intent_2);
    }

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
                    //right now the code still pulls from the local database
                    //String clean_email = LoginActivity.USER_EMAIL.replaceAll(".com", "");
                    String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                            currentUser + "/journals").child(journal_name);

                    ref.removeValue();

                    Toast.makeText(EditJournal.this, "The Journal Has Been Deleted", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(EditJournal.this, JournalActivity.class);

                    dbHandler.deleteFromJournals(getIntent().getStringExtra("Name"));
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
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                Log.d(TAG, "onClick: Yes delete the Goal");
                    System.out.println("onClick: Yes save changes to the Journal");

                    //String clean_email = LoginActivity.USER_EMAIL.replaceAll(".com", "");
                    String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                            currentUser + "/journals").child(journal_name);

                    HashMap<String, Object> changes = new HashMap<String, Object>();

                    Intent intent = new Intent(EditJournal.this, JournalActivity.class);
                    ToggleButton remindersToggle = findViewById(R.id.journal_tracking);

                    changes.put("reminders", remindersToggle.isChecked());

                    ref.updateChildren(changes);

                    dbHandler.editJournal(getIntent().getStringExtra("Name"), getIntent().getStringExtra("Description"), remindersToggle.isChecked());

//                    intent.putExtra("Name", getIntent().getStringExtra("Name"));
//                    intent.putExtra("Description", getIntent().getStringExtra("Description"));
//                    intent.putExtra("Tracking", getIntent().getBooleanExtra("Tracking", false));

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

    private void setupUI(Intent intent) {

        TextView journal_name = findViewById(R.id.journalName);

        this.journal_name = intent.getStringExtra("Name");

        journal_name.setText(this.journal_name);

        TextView journal_desc = findViewById(R.id.journal_desc_content);

        journal_desc.setText(intent.getStringExtra("Description"));


        ToggleButton remindersToggle = findViewById(R.id.journal_tracking);
        remindersToggle.setChecked(intent.getBooleanExtra("Tracking", false));

//        final MyDBHandler dbHandler = new MyDBHandler(getApplicationContext(),
//                "data_points.db", null, 1);
//        ArrayList<String> data = new ArrayList<String>();
//        data = dbHandler.getAllData(this.journal_name);



    }


}
