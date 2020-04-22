package com.example.avi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

public class LoadingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressBar progBar;
    private int prog = 0;
    private Handler handler = new Handler();

    Timer timer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        progBar = (ProgressBar) findViewById(R.id.progressBar);

        class toRun extends TimerTask{

            @Override
            public void run() {
                if(mAuth.getCurrentUser() != null){
                    Toast.makeText(getApplicationContext(), "You are signed in!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoadingActivity.this, LiveUpdates.class);
                    startActivity(intent);
                    timer.cancel();
                }
                else{
                    Intent intent = new Intent(LoadingActivity.this, ChooseLoginActivity.class);
                    startActivity(intent);
                    timer.cancel();
                }
            }
        }



        timer = new Timer();
        timer.schedule(new toRun(), 5000);
        new Thread(new Runnable() {
            public void run() {
                while (prog < 100) {
                    prog += 1;
                    // Update the progress bar and display the
                    //current value in the text view
                    handler.post(new Runnable() {
                        public void run() {
                            progBar.setProgress(prog);
                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();








    }




}
