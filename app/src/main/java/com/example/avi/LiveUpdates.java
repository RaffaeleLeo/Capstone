package com.example.avi;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.avi.Journals.JournalActivity;
import com.google.android.material.tabs.TabLayout;

import java.util.concurrent.ExecutionException;

public class LiveUpdates extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Spinner spinner;

    //***This is what the dropdown will show, update this to update options***
    private static final String[] paths = {"Traffic Updates", "Avalanche History", "Some Other Page"};

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

        //Boilerplate spinner code, no need to update any of this
        Spinner spinner = (Spinner)findViewById(R.id.spinner2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(LiveUpdates.this,
                android.R.layout.simple_spinner_item, paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
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

    //Update this method to update action of actual spinner.
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            //Go to Traffic Updates Page
        } else if (position == 1) {
            //Go to avalanche history page
        }
        else{
            //Go to other page
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

