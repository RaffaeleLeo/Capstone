package com.example.avi;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.avi.ChatRoom.ChatRoomActivity;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

public class LoadingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onResume(){
        super.onResume();
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);

        if(mAuth.getCurrentUser() != null || googleAccount != null || AccessToken.getCurrentAccessToken() != null){
            Toast.makeText(getApplicationContext(), "You are signed in!", Toast.LENGTH_SHORT).show();


            //Intent intent = new Intent(LoadingActivity.this, ChatRoomActivity.class);
            //intent.putExtra("IsFirst", true);

            Intent sIntent = new Intent(LoadingActivity.this, NotificationChecker.class);
            startService(sIntent);

            Intent intent = new Intent(LoadingActivity.this, LiveUpdates.class);
            startActivity(intent);
        }
        else{


            Intent intent = new Intent(LoadingActivity.this, ChooseLoginActivity.class);
            startActivity(intent);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        mAuth = FirebaseAuth.getInstance();

        AccessToken.getCurrentAccessToken();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("969128169123-acm8ho281ikele7r252r4urcspbf0qvs.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);

        if(mAuth.getCurrentUser() != null || googleAccount != null || AccessToken.getCurrentAccessToken() != null){
            Toast.makeText(getApplicationContext(), "You are signed in!", Toast.LENGTH_SHORT).show();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                Intent intent = new Intent(this, TrackingService.class);
                startService(intent);
            }

            //CHANGE THIS LINE?
            Intent intent = new Intent(LoadingActivity.this, LiveUpdates.class);

            startActivity(intent);
        }
        else{
            Intent intent = new Intent(LoadingActivity.this, ChooseLoginActivity.class);
            startActivity(intent);
        }







    }





}
