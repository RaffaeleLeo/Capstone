package com.example.avi;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.avi.ChatRoom.Message;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class NotificationChecker extends Service {

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mAuth;

    public NotificationChecker() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        System.exit(0);
        //System.out.println("Created Service");
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        final DatabaseReference ref = database.getReference("messages");
        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("Service recognized a data change.");
                //mAdapter.clear();
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

                    if(i == childList.size() - 1){// && currentUser) {
                        Notifications notifier = new Notifications();
                        notifier.notification("New Message from Anonymous", message, 0, getApplicationContext());
                    }

                    /*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.add(msg);
                            messageView.setSelection(messageView.getCount() - 1);
                        }
                    });

                     */
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return super.onStartCommand(intent, flags, startID);
    }

    @Override
    public void onCreate(){

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}