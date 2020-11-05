package com.example.avi.Journals;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

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

import java.util.Arrays;
import java.util.List;

import com.example.avi.LoginActivity;
import com.example.avi.MyDBHandler;
import com.example.avi.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        settingsFrag = new AddJournalActivity.SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFrag)
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        launcher = new UILauncher(this);


        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                settingsFrag.addGoal(view);
                addJournal(view, SP, dbHandler);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addJournal(View view, SharedPreferences sp, MyDBHandler dbHandler) {
        System.out.println(sp.getString("Journal_Name", "NA"));

        Journal Journal = new Journal();

        Journal.name = sp.getString("Journal_Name", "NA");
        if(Journal.name.equals("NA") || Journal.name.equals("")){
            Snackbar.make(view, "Journals of interest must have a name", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            return;
        }

        Journal.description = sp.getString("Journal_Description", "Not Set");
        if(Journal.description.equals("Not Set")){
            Journal.description = "";
        }

        Journal.start_recording = sp.getBoolean("is_tracking", false);

        // clear the goal settings after a successful
        SharedPreferences.Editor editor = sp.edit();

        //System.out.println(sp.getString(LoginActivity.EXTRA_ACCESS_EMAIL, "Not Here"));
        editor.clear();

        editor.apply();

        //add goal to firebase
        //String clean_email = LoginActivity.USER_EMAIL.replaceAll(".com", "");

        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(currentUser + "/journals");
//
//        if (Journal != null) {
//            //Save the journal data to the database//
//            ref.child(Journal.name).setValue(Journal);
//        }

        //add journal to database
        if(Journal.start_recording)
        {
            dbHandler.addToJournals(Journal.name, Journal.description, 1);
        }
        else
        {
            dbHandler.addToJournals(Journal.name, Journal.description, 0);
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