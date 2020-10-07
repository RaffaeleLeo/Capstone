package com.example.avi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.avi.ChatRoom.User;
import com.example.avi.Journals.JournalActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    ListView friends;
    ListView requests;
    Button accept;
    Button ignore;
    Button sendRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        friends = findViewById(R.id.friends_list);
        requests = findViewById(R.id.request_list);
        accept = findViewById(R.id.AcceptButton);
        ignore = findViewById(R.id.IgnoreButton);
        sendRequest = findViewById(R.id.sendRequestButton);

        final ArrayList<String> friendsList = new ArrayList<>();
        final ArrayAdapter friends_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                friendsList);
        friends.setAdapter(friends_adapter);

        final ArrayList<String> requestsList = new ArrayList<>();
        final ArrayAdapter requests_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                requestsList);
        requests.setAdapter(requests_adapter);



        DocumentReference docRef = db.collection("users").document(mAuth.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                for (String r: user.getRequests()){
                    requestsList.add(r);
                    requests_adapter.notifyDataSetChanged();
                }
                for (String f: user.getFriends()){
                    friendsList.add(f);
                    friends_adapter.notifyDataSetChanged();
                }
            }
        });


    }

    private void setupTabLayout() {
        TabLayout.OnTabSelectedListener listener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getText().equals(getString(R.string.nav_map))) {
                    Intent intent = new Intent(FriendsActivity.this, MapsActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getText().equals(getString(R.string.nav_journal))) {
                    Intent intent = new Intent(FriendsActivity.this, JournalActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                }else if (tab.getText().equals(getString(R.string.nav_live_updates))){
                    Intent intent = new Intent(FriendsActivity.this, LiveUpdates.class);

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

        tabLayout.getTabAt(3).select();
    }
}
