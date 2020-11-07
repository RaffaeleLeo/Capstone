package com.example.avi;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.avi.ChatRoom.Message;
import com.example.avi.Snapshot.SnapshotActivity;
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
    private Context context;

    public NotificationChecker() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        context = this;
        //System.out.println("Starting service");
        //System.exit(0);
        controlNotifications();

        return super.onStartCommand(intent, flags, startID);
    }



    public void controlNotifications()
    {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        final DatabaseReference ref = database.getReference("messages");

        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<DataSnapshot> childList = Lists.newArrayList(dataSnapshot.getChildren());
                for (int i = 0; i < childList.size(); i++) {

                    if (i == childList.size() - 1)
                    {
                        DataSnapshot child = childList.get(i);
                        String id = child.child("id").getValue(String.class);
                        String message = child.child("message").getValue(String.class);
                        String userName = "Anonymous";
                        boolean currentUser = false;
                        if (id.equals(mAuth.getUid())) {
                            currentUser = true;
                            userName = "Me";
                        }
                        final Message msg = new Message(message, id, userName, currentUser);
                        if(!currentUser)
                        {
                            Notifications notifier = new Notifications();
                            notifier.notification("New Message from Anonymous", message, 0, context);
                        }
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public boolean stopService (Intent service){
        return true;
        //Does nothing.
    }

    @Override
    public void onCreate(){
        //System.out.println("Starting service");
        super.onCreate();
        //System.exit(0);

    }

    @Override
    public IBinder onBind(Intent intent) {return null;}
}