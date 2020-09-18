package com.example.avi.ChatRoom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.avi.Journals.JournalActivity;
import com.example.avi.LiveUpdates;
import com.example.avi.MapsActivity;
import com.example.avi.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
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

    private DatabaseReference mFirebaseDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        setupTabLayout();

        mAdapter = new MessageAdapter(this);
        sendButton = (ImageButton) findViewById(R.id.sendButton);
        messageView = (ListView) findViewById(R.id.messages_view);
        messageView.setAdapter(mAdapter);
        editText = (EditText) findViewById(R.id.editText);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("messages");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAdapter.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String sender = child.child("sender").getValue(String.class);
                    String message = child.child("message").getValue(String.class);
                    boolean currentUser = false;
                    if(sender.equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())){
                        currentUser = true;
                    }
                    final Message msg = new Message(message, sender, currentUser);

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




        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                User usr1 = new User("User1");
//                User usr2 = new User("Me");
//
//                final Message msg1 = new Message("Hello", usr1, false);
//                final Message msg2 = new Message("Hi", usr2, true);

                final Message msg = new Message(editText.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), true);
                ref.push().setValue(msg);
                editText.setText("");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        mAdapter.add(msg1);
////                        messageView.setSelection(messageView.getCount() - 1);
////                        mAdapter.add(msg2);
////                        messageView.setSelection(messageView.getCount() - 1);
//                    }
//                });
//
//                editText.setText("");
            }
        });
    }

    /**
     * sets up the tab layout at the bottom of the screen
     */
    private void setupTabLayout() {
        TabLayout.OnTabSelectedListener listener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getText().equals(getString(R.string.nav_map))) {
                    Intent intent = new Intent(ChatRoomActivity.this, MapsActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getText().equals(getString(R.string.nav_journal))) {
                    Intent intent = new Intent(ChatRoomActivity.this, JournalActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                }else if (tab.getText().equals(getString(R.string.nav_live_updates))){
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
