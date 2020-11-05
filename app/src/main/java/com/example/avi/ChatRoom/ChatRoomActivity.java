package com.example.avi.ChatRoom;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.avi.Journals.JournalActivity;
import com.example.avi.LiveUpdates;
import com.example.avi.LoadingActivity;
import com.example.avi.MapsActivity;
import com.example.avi.Notifications;
import com.example.avi.R;
import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {

    ImageButton sendButton;
    MessageAdapter mAdapter;
    ListView messageView;
    EditText editText;
    Boolean isVisible;

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart(){
        super.onStart();
        isVisible = true;
    }

    @Override
    protected void onStop(){
        super.onStop();
        isVisible = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        isVisible = true;
        setContentView(R.layout.activity_chat_room);
        setupTabLayout();

        mAdapter = new MessageAdapter(this);
        messageView = (ListView) findViewById(R.id.messages_view);
        messageView.setAdapter(mAdapter);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        final DatabaseReference ref = database.getReference("messages");
        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAdapter.clear();
                List<DataSnapshot> childList = Lists.newArrayList(dataSnapshot.getChildren());
                for(int i = 0; i<childList.size(); i++) {
                    DataSnapshot child = childList.get(i);
                    String id = child.child("id").getValue(String.class);
                    String message = child.child("message").getValue(String.class);
                    String userName = "Anonymous";
                    boolean currentUser = false;
                    if(id.equals(mAuth.getUid())){
                        currentUser = true;
                        userName = "Me";
                    }
                    final Message msg = new Message(message, id, userName, currentUser);

                    /*
                    if(i == childList.size() - 1 && !currentUser && !isVisible && !getIntent().getBooleanExtra("IsFirst", false)) {
                        Notifications notifier = new Notifications();
                        notifier.notification("New Message from Anonymous", message, 0, getApplicationContext());
                    }

                     */

                    runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.add(msg);
                        messageView.setSelection(messageView.getCount() - 1);
                    }
                });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        //The first time this method is called is when the app is starting up. The app will go here first and then to the proper starting point.
        //That way, the notifications for chat messages will begin.
        if(getIntent().getBooleanExtra("IsFirst", false))
        {
            Intent intent = new Intent(ChatRoomActivity.this, LiveUpdates.class);

            startActivity(intent);
        }
    }

    /**
     * sets up the tab layout at the bottom of the screen
     */
    private void setupTabLayout() {
        TabLayout.OnTabSelectedListener listener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 1) {
                    Intent intent = new Intent(ChatRoomActivity.this, MapsActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 2) {
                    Intent intent = new Intent(ChatRoomActivity.this, JournalActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                }else if (tab.getPosition() == 0){
                    Intent intent = new Intent(ChatRoomActivity.this, LiveUpdates.class);

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
