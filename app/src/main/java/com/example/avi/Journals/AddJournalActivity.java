package com.example.avi.Journals;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.provider.Contacts;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.avi.LoginActivity;
import com.example.avi.MyDBHandler;
import com.example.avi.Notifications;
import com.example.avi.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddJournalActivity extends AppCompatActivity {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private AddJournalActivity.SettingsFragment settingsFrag;
    public static SharedPreferences SP;
    public static AddJournalActivity.UILauncher launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        final MyDBHandler dbHandler = new MyDBHandler(getApplicationContext(), "journals.db", null, 1);

        //set up the add journal fragment
        settingsFrag = new AddJournalActivity.SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFrag)
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //get the context from the fragment
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        launcher = new UILauncher(this);


        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addJournal(view, SP, dbHandler);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addJournal(View view, SharedPreferences sp, MyDBHandler dbHandler) {
        System.out.println(sp.getString("Journal_Name", "NA"));
        Journal Journal = new Journal();

        //use the shared preferences from the xml fragment to get what the user input
        Journal.name = sp.getString("Journal_Name", "NA");
        if (Journal.name.equals("NA") || Journal.name.equals("")) {
            Snackbar.make(view, "Journals of interest must have a name", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            return;
        }

        ArrayList<Journal> currentJournals = dbHandler.getAllJournals();
        for(int i = 0; i < currentJournals.size(); i++){
            Journal oneJournal = currentJournals.get(i);
            if(oneJournal.name.equals(Journal.name)){
                Snackbar.make(view, "Journal cannot have the same name as another journal", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                return;
            }
        }

        Journal.type = sp.getString("type", "NA");
        if(Journal.type.equals("NA") || Journal.type.equals("")){
            Snackbar.make(view, "Please Choose a Type for your Goal", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            return;

        }



        Journal.description = sp.getString("Journal_Description", "Not Set");
        if(Journal.description.equals("Not Set")){
            Journal.description = "";
        }

        Journal.start_recording = sp.getBoolean("is_tracking", false);

        // clear the goal settings after a successful save
        SharedPreferences.Editor editor = sp.edit();

        //System.out.println(sp.getString(LoginActivity.EXTRA_ACCESS_EMAIL, "Not Here"));
        editor.clear();

        editor.apply();


        //add journal to database
        if(Journal.start_recording) {
            dbHandler.addToJournals(Journal.name, Journal.description, 1, Journal.type);
            Notifications notifier = new Notifications();
            notifier.notification("Route tracking started.", "Your location will be periodically recorded.", (int) System.currentTimeMillis(),this);
        }
        else
        {
            dbHandler.addToJournals(Journal.name, Journal.description, 0, Journal.type);
        }

        // Go back to the journals tab
        Intent intent = new Intent(AddJournalActivity.this, JournalActivity.class);
        intent.putExtra("Name", Journal.name);
        intent.putExtra("Description", Journal.description);
        intent.putExtra("Tracking", Journal.start_recording);
        startActivity(intent);
    }


    public class UILauncher{
        public AddJournalActivity active;
        public Preference editTextPreference;

        UILauncher(AddJournalActivity active){
            this.active = active;

        }

        public void setEditTextPreferenceText(String placeName){

            editTextPreference.setTitle(placeName);
        }

    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.new_journal_settings, rootKey);

            this.findPreference("Journal");

        }

        public void addJournal(View view) {

            findPreference("Journal_Name");

        }
    }

}