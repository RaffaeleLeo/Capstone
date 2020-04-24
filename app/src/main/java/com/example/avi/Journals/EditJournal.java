package com.example.avi.Journals;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

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

public class EditJournal extends AppCompatActivity {

    private String journal_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journal);

        Intent intent = getIntent();


        setupUI(intent);

        Button save_button = findViewById(R.id.saveButton);

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog("save");

            }
        });

        Button delete_button = findViewById(R.id.deleteButton);

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createDialog("delete");


            }
        });

    }

    private void createDialog(String action) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);


        if(action.equals("delete")) {
            alertDialog.setMessage("Are You Sure You Want To Delete This Goal?");
            alertDialog.setTitle("Delete Goal");

            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                Log.d(TAG, "onClick: Yes delete the Goal");
                    System.out.println("onClick: Yes delete the Goal");

                   //TODO: connect to database

                    Toast.makeText(EditJournal.this, "The Goal Has Been Deleted", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(EditJournal.this, JournalActivity.class);

                    startActivity(intent);
                }
            });

            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.out.println("onClick: No do not delete the Goal");
                }
            });
        }else{

            alertDialog.setMessage("Save Changes to Current Goal?");
            alertDialog.setTitle("Save Changes");


            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                Log.d(TAG, "onClick: Yes delete the Goal");
                    System.out.println("onClick: Yes save changes to the Goal");

                    //TODO: CONNECT TO A DATABASE



                    Intent intent = new Intent(EditJournal.this, JournalActivity.class);

                    intent.putExtra("Name", getIntent().getStringExtra("Name"));
                    intent.putExtra("Description", getIntent().getStringExtra("Description"));
                    intent.putExtra("Tracking", getIntent().getBooleanExtra("Tracking", false));

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

    }


}
