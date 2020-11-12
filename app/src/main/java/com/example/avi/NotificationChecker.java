package com.example.avi;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.avi.ChatRoom.Message;
import com.example.avi.ChatRoom.User;
import com.example.avi.Snapshot.SnapshotActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class NotificationChecker extends Service {

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mAuth;
    private Context context;
    private FirebaseFirestore db;

    private String email;



    public NotificationChecker() {
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        context = this;
        //System.out.println("Starting service");
        //System.exit(0);
        email = intent.getStringExtra("email");

        controlNotifications();

        return super.onStartCommand(intent, flags, startID);
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void controlNotifications()
    {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


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
                        if(!currentUser && !getApplicationContext().getSharedPreferences("Prefs", 0).getBoolean("AreInChatRoom", false))
                        {

                            if(!(getApplicationContext().getSharedPreferences("Prefs", 0).getString("LastMessage", "").equals( id))) {
                                SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("LastMessage", id);
                                editor.commit();

                                Notifications notifier = new Notifications();
                                notifier.notification("New Message from Anonymous", message, (int) System.currentTimeMillis(), context);
                            }
                         }
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        //final String[] emailAddress = {""};
        DocumentReference docRef =db.collection("users").document(mAuth.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                User user = documentSnapshot.toObject(User.class);
                HashMap<String, String> requests = user.getRequests();
                List<String> requestList = new ArrayList<String>(requests.values());
                //emailAddress[0] = user.getEmail();
                //Notifications notifier1 = new Notifications();
                //notifier1.notification(emailAddress[0], email, (int) System.currentTimeMillis(), context);

                if(requestList.size() != 0  && !getApplicationContext().getSharedPreferences("Prefs", 0).getBoolean("AreInFriends", false)) {
                    if(!(getApplicationContext().getSharedPreferences("Prefs", 0).getString("LastFriendRequest", "").equals( requestList.get(requestList.size() - 1))))
                    {
                        SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("LastFriendRequest", requestList.get(requestList.size() - 1) );
                        editor.commit();

                        Notifications notifier = new Notifications();
                        notifier.notification("New friend request.", requestList.get(requestList.size() - 1), (int) System.currentTimeMillis(), context);
                    }
                }
            }
        });

        /*
        //final String[] emailAddress = {""};
        final DocumentReference userDocRef = db.collection("users").document(email);
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                if (user == null) {
                    Log.d("user", "not found");
                }
                emailAddress[0] = user.getEmail();

            }
        });

         */

        //Notifications notifier = new Notifications();
        //notifier.notification(emailAddress[0], email, (int) System.currentTimeMillis(), context);







        db.collection("tours")
                .whereArrayContains("pendingInvitees", email)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if(!snapshots.isEmpty() && !getApplicationContext().getSharedPreferences("Prefs", 0).getBoolean("AreInTours", false)) {

                            //snapshots.getDocuments().get(snapshots.getDocuments().size() - 1).getId()
                            if(!(getApplicationContext().getSharedPreferences("Prefs", 0).getString("LastTourInvite", "").equals( snapshots.getDocuments().get(snapshots.getDocuments().size() - 1).getId())))
                            {
                                SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("LastTourInvite", snapshots.getDocuments().get(snapshots.getDocuments().size() - 1).getId());
                                editor.commit();

                                Notifications notifier = new Notifications();
                                notifier.notification("New tour invite."  , "Invitation to tour \"" + snapshots.getDocuments().get(snapshots.getDocuments().size() - 1).getString("tourName") + "\"."/* + "from "+ snapshots.getDocuments().get(snapshots.getDocuments().size() - 1).getString("tour") +"."*/, (int) System.currentTimeMillis(), context);
                            }
                        }
                }
            });



    }

    @Override
    public boolean stopService (Intent service){
        return true;
        //Does nothing - service will never stop ever. Hehehehehehe!
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