package com.example.avi;

import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

   Button loginButton, createButton;
   EditText createFirst, createLast, createEmail, createPassword, loginEmail, loginPassword;


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

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString().toLowerCase();
                String password = loginPassword.getText().toString();

                String actualPassword = dbHandler.findUser(email, password);

                if(email.equals("") || password.equals("")){
                    Toast.makeText(getApplicationContext(), "All fields must be filled out.", Toast.LENGTH_LONG).show();
                }

                else{
                    if (password.equals(actualPassword)) {

                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "User not found, please try again", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String first = createFirst.getText().toString();
                String last = createLast.getText().toString();
                String email = createEmail.getText().toString().toLowerCase();
                String password = createPassword.getText().toString();

                if(first.equals("") || last.equals("") || email.equals("") || password.equals("")){
                    Toast.makeText(getApplicationContext(), "All fields must be filled out.", Toast.LENGTH_LONG).show();
                }

                else if (password.length() < 6){
                    Toast.makeText(getApplicationContext(), "Password must be at least 6 characters long.", Toast.LENGTH_LONG).show();
                }

                else {

                    try {

                        dbHandler.addToUsers(email, first, last, password, true);
                        Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                        startActivity(intent);


                    } catch (SQLException e) {
                        Toast.makeText(getApplicationContext(), "An account with this email already exists, please login using your created account.", Toast.LENGTH_LONG).show();
                    }

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
}
