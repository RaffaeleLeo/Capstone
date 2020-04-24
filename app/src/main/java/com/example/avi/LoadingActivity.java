package com.example.avi;

import android.app.Activity;
import android.app.job.JobInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

public class LoadingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


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
            Intent intent = new Intent(LoadingActivity.this, JournalActivity.class);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(LoadingActivity.this, ChooseLoginActivity.class);
            startActivity(intent);
        }







    }





}
