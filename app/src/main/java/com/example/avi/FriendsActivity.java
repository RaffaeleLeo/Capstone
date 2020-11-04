package com.example.avi;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avi.ChatRoom.User;
import com.example.avi.Journals.JournalActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    ListView friends;
    ListView requests;
    Button accept;
    Button ignore;
    Button sendRequest;
    EditText emailField;

    ArrayList<String> friendsTracker;
    ArrayList<String> requestsTracker;

    String lastClicked;

    View pop_up_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        final Button pop_up = findViewById(R.id.show_popup);

        pop_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater inflater = getLayoutInflater();
                pop_up_view = inflater.inflate(R.layout.add_frineds_popup, null);

                sendRequest = pop_up_view.findViewById(R.id.sendRequestButton2);
                emailField = pop_up_view.findViewById(R.id.sendRequestInput2);

                sendRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String email = emailField.getText().toString().toLowerCase().trim();
                        if(!(email.equals(mAuth.getCurrentUser().getEmail()))) {
                            db.collection("users").whereEqualTo("email", email).get()
                                    .addOnCompleteListener((new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    document.getId();
                                                    String id = mAuth.getUid();
                                                    String dest = "requests." + id;
                                                    db.collection("users").document(document.getId())
                                                            .update(
                                                                    dest, mAuth.getCurrentUser().getDisplayName()
                                                            );
                                                    emailField.setText("");
                                                    Toast.makeText(getApplicationContext(), "Request Sent", Toast.LENGTH_LONG).show();
                                                }
                                            } else {
                                                Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_LONG).show();
                                                emailField.setText("");
                                            }
                                        }
                                    }));
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Cannot send friend request to self", Toast.LENGTH_LONG).show();
                            emailField.setText("");
                        }
                    }
                });


                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                builder.setView(pop_up_view);
                AlertDialog alertDialog = builder.create();
                builder.show();


            }
        });

        friends = findViewById(R.id.friends_list);
        requests = findViewById(R.id.request_list);
        accept = findViewById(R.id.AcceptButton);
        ignore = findViewById(R.id.IgnoreButton);
        //sendRequest = findViewById(R.id.sendRequestButton);
        //emailField = findViewById(R.id.sendRequestInput);

        final ArrayList<String> friendsList = new ArrayList<>();
        friendsTracker = new ArrayList<>();
        final ArrayAdapter friends_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                friendsList);
        friends.setAdapter(friends_adapter);

        final ArrayList<String> requestsList = new ArrayList<>();
        requestsTracker = new ArrayList<>();
        final ArrayAdapter requests_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                requestsList);
        requests.setAdapter(requests_adapter);


        DocumentReference docRef = db.collection("users").document(mAuth.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                if(user != null)
                {
                    for (Map.Entry<String, String> entry : user.getRequests().entrySet()) {
                        requestsList.add(entry.getValue());
                        requestsTracker.add(entry.getKey());
                        requests_adapter.notifyDataSetChanged();
                    }

                    for (Map.Entry<String, String> entry : user.getFriends().entrySet()) {
                        friendsList.add(entry.getValue());
                        friendsTracker.add(entry.getKey());
                        friends_adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        requests.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastClicked = (String) parent.getItemAtPosition(position);


            }

        });


        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lastClicked != null){
                    int index = requestsList.indexOf(lastClicked);
                    lastClicked = null;
                    if(index >= 0) {
                        String id = requestsTracker.get(index);
                        String name = requestsList.get(index);
                        requestsList.remove(index);
                        requestsTracker.remove(index);
                        requests_adapter.notifyDataSetChanged();
                        friendsList.add(name);
                        friendsTracker.add(name);
                        friends_adapter.notifyDataSetChanged();

                        //Add to current user's friends
                        String dest = "friends." + id;
                        db.collection("users").document(mAuth.getUid())
                                .update(
                                        dest, name
                                );


                        //Remove request from current user
                        dest = "requests." + id;
                        db.collection("users").document(mAuth.getUid())
                                .update(
                                        dest, FieldValue.delete()
                                );

                        //Add to requesting user's friends
                        db.collection("users").whereEqualTo("id", id).get()
                                .addOnCompleteListener((new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String id = document.getId();
                                                String dest = "friends." + mAuth.getUid();
                                                db.collection("users").document(id)
                                                        .update(
                                                                dest, mAuth.getCurrentUser().getDisplayName()
                                                        );
                                            }
                                        } else {
                                        }
                                    }
                                }));
                    }
                }
            }
        });

        ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lastClicked != null) {
                    int index = requestsList.indexOf(lastClicked);
                    lastClicked = null;
                    if (index >= 0) {
                        String id = requestsTracker.get(index);
                        requestsList.remove(index);
                        requestsTracker.remove(index);
                        requests_adapter.notifyDataSetChanged();

                        //Remove request from current user
                        String dest = "requests." + id;
                        db.collection("users").document(mAuth.getUid())
                                .update(
                                        dest, FieldValue.delete()
                                );
                    }
                }
            }
        });
//        sendRequest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final String email = emailField.getText().toString().toLowerCase().trim();
//                if(!(email.equals(mAuth.getCurrentUser().getEmail()))) {
//                    db.collection("users").whereEqualTo("email", email).get()
//                            .addOnCompleteListener((new OnCompleteListener<QuerySnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                    if (task.isSuccessful()) {
//                                        for (QueryDocumentSnapshot document : task.getResult()) {
//                                            document.getId();
//                                            String id = mAuth.getUid();
//                                            String dest = "requests." + id;
//                                            db.collection("users").document(document.getId())
//                                                    .update(
//                                                            dest, mAuth.getCurrentUser().getDisplayName()
//                                                    );
//                                            emailField.setText("");
//                                            Toast.makeText(getApplicationContext(), "Request Sent", Toast.LENGTH_LONG).show();
//                                        }
//                                    } else {
//                                        Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_LONG).show();
//                                        emailField.setText("");
//                                    }
//                                }
//                            }));
//                }
//                else {
//                    Toast.makeText(getApplicationContext(), "Cannot send friend request to self", Toast.LENGTH_LONG).show();
//                    emailField.setText("");
//                }
//            }
//        });

        setupTabLayout();
    }

    /**
     * sets up the tab layout at the bottom of the screen
     */
    private void setupTabLayout() {
        TabLayout tabLayout = findViewById(R.id.TabLayout);
        tabLayout.getTabAt(3).select();
        TabLayout.OnTabSelectedListener listener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 2) {
                    Intent intent = new Intent(FriendsActivity.this, JournalActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 3) {
                    Intent intent = new Intent(FriendsActivity.this, SocialMediaHomeActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 0) {
                    Intent intent = new Intent(FriendsActivity.this, LiveUpdates.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 1) {
                    Intent intent = new Intent(FriendsActivity.this, MapsActivity.class);

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
