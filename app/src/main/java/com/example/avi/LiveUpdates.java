package com.example.avi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.concurrent.ExecutionException;

public class LiveUpdates extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_updates);
        setupTabLayout();
        try{
        updateWeatherData();
        }catch (Exception e){

        }

        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.loadUrl("https://utahavalanchecenter.org/forecast/salt-lake");
    }



    private void updateWeatherData() throws ExecutionException, InterruptedException {
        WeatherData data = new WeatherData();
        data.execute("hi");
        WeatherData.Weather weather = (WeatherData.Weather) data.get().get(0);
        TextView text = findViewById(R.id.textView);
        text.setText(weather.toString());
    }

    private void setupTabLayout() {
        TabLayout.OnTabSelectedListener listener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getText().equals(getString(R.string.nav_map))) {
                    Intent intent = new Intent(LiveUpdates.this, MapsActivity.class);

                    startActivity(intent);
                } else if (tab.getText().equals(getString(R.string.nav_chat))) {
                    Intent intent = new Intent(LiveUpdates.this, ChatRoomActivity.class);

                    startActivity(intent);
                } else if (tab.getText().equals(getString(R.string.nav_journal))) {
                    Intent intent = new Intent(LiveUpdates.this, JournalActivity.class);

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

        tabLayout.getTabAt(0).select();
    }
}
