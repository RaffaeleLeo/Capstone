package com.example.avi;

import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

   Button loginButton, createButton;
   EditText createFirst, createLast, createEmail, createPassword, loginEmail, loginPassword;

   private FirebaseAuth mAuth;
   public static final String EXTRA_ACCESS_ID = "com.auth0.ACCESS_ID";

    public static String USER_EMAIL = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        createFirst = (EditText) findViewById(R.id.CreateLastName);
        createLast = (EditText) findViewById(R.id.CreateFirstName);
        createEmail = (EditText) findViewById(R.id.CreateEmail);
        createPassword = (EditText) findViewById(R.id.CreatePassword);
        loginEmail = (EditText) findViewById(R.id.LoginEmail);
        loginPassword = (EditText) findViewById(R.id.LoginPassword);

        loginButton = (Button)findViewById(R.id.LoginButton);
        createButton = (Button)findViewById(R.id.CreateButton);

        final MyDBHandler dbHandler = new MyDBHandler(getApplicationContext(), "users.db", null, 1);
        mAuth = FirebaseAuth.getInstance();


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString().toLowerCase();
                String password = loginPassword.getText().toString();


                if (email.equals("") || password.equals("")) {
                    Toast.makeText(getApplicationContext(), "All fields must be filled out.", Toast.LENGTH_LONG).show();
                } else {
                    loginUser(email, password, dbHandler);
                }


            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String first = createFirst.getText().toString();
                String last = createLast.getText().toString();
                String email = createEmail.getText().toString().toLowerCase();
                USER_EMAIL = email;
                String password = createPassword.getText().toString();

                if (first.equals("") || last.equals("") || email.equals("") || password.equals("")) {
                    Notifications notifier = new Notifications();
                    notifier.notification("test", "this is a test", 0, LoginActivity.this);

                    Toast.makeText(getApplicationContext(), "All fields must be filled out.", Toast.LENGTH_LONG).show();
                } else if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password must be at least 6 characters long.", Toast.LENGTH_LONG).show();
                } else {
                    registerUser(email, password, first + " " + last, dbHandler);
                }





            }
        });


        //setupTabLayout();
    }




    /**
     * sets up the tab layout at the bottom of the screen
     */
/*    private void setupTabLayout() {
        TabLayout.OnTabSelectedListener listener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getText().equals(getString(R.string.nav_map))) {
                    Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                    // TODO pass along the credentials
//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getText().equals(getString(R.string.nav_chat))) {
                    Intent intent = new Intent(LoginActivity.this, ChatRoomActivity.class);
                    // TODO pass along the credentials
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

        tabLayout.getTabAt(1).select();
    }*/

    private void registerUser(String email, String password, final String displayName, final MyDBHandler dbHandler){
        USER_EMAIL = email;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName).build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(LoginActivity.this, LiveUpdates.class);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                            Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, LiveUpdates.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Email already associated with account, please log in.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void loginUser(String email, String password, final MyDBHandler dbHandler){
        USER_EMAIL = email;
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            try {
                                dbHandler.addToUsers(user.getUid(), user.getDisplayName(), user.getEmail(), "Email");
                            }
                            catch (Exception e){
                            }
                            Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, LiveUpdates.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "User / password combination not found, please try again", Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }
}
