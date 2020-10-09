package com.example.avi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.avi.ChatRoom.ChatRoomActivity;
import com.example.avi.Journals.JournalActivity;
import com.google.android.material.tabs.TabLayout;

public class SocialMediaHomeActivity extends AppCompatActivity {

    Button settings;
    Button chat;
    Button friends;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media_home);

        settings = (Button) findViewById(R.id.gotoSettings);
        chat = (Button) findViewById(R.id.gotoChat);
        friends = (Button) findViewById(R.id.gotoFriends);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SocialMediaHomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SocialMediaHomeActivity.this, ChatRoomActivity.class);
                startActivity(intent);
            }
        });

        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SocialMediaHomeActivity.this, FriendsActivity.class);
                startActivity(intent);
            }
        });
        setupTabLayout();

    }

    private void setupTabLayout() {
        TabLayout.OnTabSelectedListener listener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getText().equals(getString(R.string.nav_map))) {
                    Intent intent = new Intent(SocialMediaHomeActivity.this, MapsActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                } else if (tab.getText().equals(getString(R.string.nav_journal))) {
                    Intent intent = new Intent(SocialMediaHomeActivity.this, JournalActivity.class);

//                    intent.putExtra(LoginActivity.EXTRA_ACCESS_AUTHENTICATED, credentials.getAccessToken());
                    startActivity(intent);
                }else if (tab.getText().equals(getString(R.string.nav_live_updates))){
                    Intent intent = new Intent(SocialMediaHomeActivity.this, LiveUpdates.class);

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



