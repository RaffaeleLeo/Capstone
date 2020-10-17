package com.example.avi.Journals;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avi.LoginActivity;
import com.example.avi.MyDBHandler;
import com.example.avi.SettingsActivity;
import com.example.avi.SocialMediaHomeActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.avi.ChatRoom.ChatRoomActivity;
import com.example.avi.LiveUpdates;
import com.example.avi.MapsActivity;
import com.example.avi.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class JournalActivity extends AppCompatActivity implements JournalAdapter.JournalViewHolder.OnJournalListener{
    Button settings;
    private RecyclerView recyclerView;
    private JournalAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Journal> Journals;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        setTitle("Journal");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { ;
                Intent intent = new Intent(JournalActivity.this, AddJournalActivity.class);
                startActivity(intent);
            }
        });
        final MyDBHandler dbHandler = new MyDBHandler(getApplicationContext(), "journals.db", null, 1);




        setupTabLayout();
        settings = findViewById(R.id.topBar).findViewById(R.id.settingsButton);
        TextView title = (TextView) findViewById(R.id.topBar).findViewById(R.id.pageTitle);
        title.setText("Journals");

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JournalActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.journal_recycle);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Journals = new ArrayList<Journal>();

        //IF WE ARE USING FIREBASE THIS WILL BE USEFUL
        /**
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(LoginActivity.USER_EMAIL + "/Journals");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, HashMap<String, Object>> journal_data =  (HashMap<String, HashMap<String, Object>>) dataSnapshot.getValue();

                if(journal_data == null)
                {
                    return;
                }

                for(String POI_name : journal_data.keySet()){
                    mAdapter.addJournal(journal_data.get(Journal_name));
                }
                mAdapter.notifyDataSetChanged();
//                System.out.println(Jornals.keySet());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        **/
        Journals = dbHandler.getAllJournals();
        mAdapter = new JournalAdapter(Journals, this);

        recyclerView.setAdapter(mAdapter);

    }


    @Override
    protected void onResume() {
        super.onResume();
        TabLayout tabLayout = findViewById(R.id.TabLayout);
        tabLayout.getTabAt(2).select();

        Intent intent = getIntent();

//        Journal j = new Journal();
//        j.description = intent.getStringExtra("Name");
//        j.name = intent.getStringExtra("Description");
//        j.start_recording = intent.getBooleanExtra("Tracking", false);
//        j.data_points = null;

//        if(j.description != null)
//        {
//            Journals.add(j);
//        }


        for(int i = 0; i < Journals.size(); i++){

            JournalAdapter.JournalViewHolder holder = (JournalAdapter.JournalViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if(holder != null){
                holder.itemView.setBackgroundResource(R.drawable.border);
            }

        }

    }

    @Override
    public void onJournalClick(int position) {

        recyclerView.findViewHolderForAdapterPosition(position).itemView
                .setBackgroundResource(R.drawable.button_dark_blue);



        Journal Journal = Journals.get(position);

        Intent intent = new Intent(JournalActivity.this, EditJournal.class);

        intent.putExtra("Name", Journal.name);
        intent.putExtra("Description", Journal.description);
        intent.putExtra("Tracking", Journal.start_recording);


        startActivity(intent);
    }



    /**
     * sets up the tab layout at the bottom of the screen
     */
    private void setupTabLayout() {
        TabLayout.OnTabSelectedListener listener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 1) {
                    Intent intent = new Intent(JournalActivity.this, MapsActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getPosition() == 3) {
                    Intent intent = new Intent(JournalActivity.this, SocialMediaHomeActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                }else if (tab.getPosition() == 0){
                    Intent intent = new Intent(JournalActivity.this, LiveUpdates.class);

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

        tabLayout.getTabAt(2).select();
    }

}
