package com.example.avi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.avi.ChatRoom.ChatRoomActivity;
import com.example.avi.ChatRoom.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChooseLoginActivity extends AppCompatActivity {

    Button emailButton;

    private FirebaseFirestore db;

    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;

    private CallbackManager mCallbackManager;

    private MyDBHandler dbHandler;

    Button pseudoFacebook;

    private static final int PERMISSIONS_REQUEST = 100;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_method);

        dbHandler = new MyDBHandler(getApplicationContext(), "snapshot.db", null, 1);

        //facebookButton = (ImageButton) findViewById(R.id.facebookButton);
        Button googleButton = findViewById(R.id.googleButton);

        emailButton = findViewById(R.id.emailButton);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("969128169123-acm8ho281ikele7r252r4urcspbf0qvs.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        mCallbackManager = CallbackManager.Factory.create();

        db = FirebaseFirestore.getInstance();

        getLocationTracking();

        final LoginButton facebookButton = findViewById(R.id.facebookButton);
        pseudoFacebook = findViewById(R.id.facebook_button);
        pseudoFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    facebookButton.performClick();
            }
        });

        facebookButton.setPermissions("email", "public_profile");
        facebookButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "Authentication failed.",
                        Toast.LENGTH_LONG).show();
            }
        });
        // [END initialize_fblogin]



        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 9001);
            }
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseLoginActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = (GoogleSignInAccount) ((Task) task).getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (Throwable e) {
                Toast.makeText(getApplicationContext(), "Login failed.", Toast.LENGTH_LONG).show();

            }
        }
        else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser user = mAuth.getCurrentUser();

                            DocumentReference docRef = db.collection("users").document(user.getUid());
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                        } else {
                                            HashMap<String, String> friends = new HashMap<>();
                                            //friends.put("0000", "Jim");
                                            HashMap<String, String> requests = new HashMap<>();
                                            //requests.put("2345", "Ted");
                                            User usr = new User(user.getUid(), user.getDisplayName(), user.getEmail(), friends, requests);
                                            db.collection("users").document(usr.getId()).set(usr);
                                        }
                                    } else {
                                    }
                                }
                            });

//                            HashMap<String, String> friends = new HashMap<>();
//                            //friends.put("9999", "John");
//                            HashMap<String, String> requests = new HashMap<>();
//                            //requests.put("1234", "Bill");
//                            User usr = new User(user.getUid(), user.getDisplayName(), user.getEmail(), friends, requests);
//                            db.collection("users").document(usr.getId()).set(usr);
                            dbHandler.clearSnapshotTable();
                            Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
//                            Intent intent = new Intent(ChooseLoginActivity.this, ChatRoomActivity.class);
//                            intent.putExtra("IsFirst", true);
                            Intent sIntent = new Intent(ChooseLoginActivity.this, NotificationChecker.class);
                            startService(sIntent);

                            Intent intent = new Intent(ChooseLoginActivity.this, LiveUpdates.class);

                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_LONG).show();
                        }


                    }
                });
    }

    private void getLocationTracking(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(this, TrackingService.class);
            startService(intent);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this, TrackingService.class);
            startService(intent);
        } else {
            AlertDialog alert = new AlertDialog.Builder(this).create();
            alert.setMessage("Many app features like route recording, avi prediction, and group member " +
                    "tracking will not work without location permissions");
            alert.show();
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser user = mAuth.getCurrentUser();

                            DocumentReference docRef = db.collection("users").document(user.getUid());
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                        } else {
                                            HashMap<String, String> friends = new HashMap<>();
                                            //friends.put("0000", "Jim");
                                            HashMap<String, String> requests = new HashMap<>();
                                            //requests.put("2345", "Ted");
                                            User usr = new User(user.getUid(), user.getDisplayName(), user.getEmail(), friends, requests);
                                            db.collection("users").document(usr.getId()).set(usr);
                                        }
                                    } else {
                                    }
                                }
                            });

//                            HashMap<String, String> friends = new HashMap<>();
//                            //friends.put("0000", "Jim");
//                            HashMap<String, String> requests = new HashMap<>();
//                            //requests.put("2345", "Ted");
//                            User usr = new User(user.getUid(), user.getDisplayName(), user.getEmail(), friends, requests);
//                            db.collection("users").document(usr.getId()).set(usr);
                            dbHandler.clearSnapshotTable();
                            Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
//                            Intent intent = new Intent(ChooseLoginActivity.this, ChatRoomActivity.class);
//                            intent.putExtra("IsFirst", true);
                            Intent sIntent = new Intent(ChooseLoginActivity.this, NotificationChecker.class);
                            startService(sIntent);

                            Intent intent = new Intent(ChooseLoginActivity.this, LiveUpdates.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }



}
