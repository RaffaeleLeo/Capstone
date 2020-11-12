package com.example.avi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.avi.Journals.JournalActivity;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;



public class SettingsActivity extends AppCompatActivity {

    Button logOutButton;
    Switch notifications;
    EditText hours;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onResume(){
        super.onResume();
        TabLayout tabLayout = findViewById(R.id.TabLayout);
        tabLayout.getTabAt(getIntent().getIntExtra("PRIOR", 0)).select();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("969128169123-acm8ho281ikele7r252r4urcspbf0qvs.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        logOutButton = (Button) findViewById(R.id.LogOutButton);
        notifications = (Switch) findViewById(R.id.notificationSwitch);
        notifications.setChecked(getApplicationContext().getSharedPreferences("Prefs", 0).getBoolean("AllowNotifications", true));

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                AccessToken.setCurrentAccessToken(null);
                mGoogleSignInClient.signOut();

                Toast.makeText(getApplicationContext(), "You have logged out.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SettingsActivity.this, LoadingActivity.class);
                startActivity(intent);
            }
        });

        notifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("AllowNotifications", isChecked);
                editor.commit();
            }
        });

        hours = (EditText) findViewById(R.id.hourCount);
        hours.setText(getApplicationContext().getSharedPreferences("Prefs", 0).getString("notifyHours", "1"));

        hours.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int hourNumber = Integer.parseInt(charSequence.toString());
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("notifyHours", charSequence.toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



        setupTabLayout();
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
                    Intent intent = new Intent(SettingsActivity.this, JournalActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 3) {
                    Intent intent = new Intent(SettingsActivity.this, SocialMediaHomeActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 0) {
                    Intent intent = new Intent(SettingsActivity.this, LiveUpdates.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 1) {
                    Intent intent = new Intent(SettingsActivity.this, MapsActivity.class);

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
