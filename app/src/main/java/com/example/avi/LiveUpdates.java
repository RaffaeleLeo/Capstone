package com.example.avi;


import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.example.avi.ChatRoom.ChatRoomActivity;
import com.example.avi.Journals.JournalActivity;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class LiveUpdates extends Activity {

    private ViewPager viewPager;
    private MyPagerAdapter adapter;
    private List<View> webViewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_updates);
        setupTabLayout();

        adapter = new MyPagerAdapter();
        viewPager = findViewById(R.id.pager);

        webViewList = new ArrayList<View>();
        addWebView(webViewList, "https://utahavalanchecenter.org/forecast/salt-lake");
        addWebView(webViewList, "https://utahavalanchecenter.org/avalanches");
        addWebView(webViewList, "https://cottonwoodcanyons.udot.utah.gov/canyon-road-information/");

        viewPager.setAdapter(adapter);
    }

    private void addWebView(List<View> viewList, String url)
    {
        WebView webView=new WebView(this);
        webView.loadUrl(url);
        viewList.add(webView);
    }

    /* preserving for future
    private void updateWeatherData() {
        try {
            WeatherData data = new WeatherData();
            data.execute("hi");
            WeatherData.Weather weather = (WeatherData.Weather) data.get().get(0);
            TextView text = findViewById(R.id.textView);
            text.setText(weather.toString());
        } catch (Exception e){
            TextView text = findViewById(R.id.textView);
            text.setText("Weather data could not be obtained");
        }
    }
*/
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

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            (container).removeView(webViewList.get(position));
        }

        @Override
        public int getCount() {
            return webViewList.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(webViewList.get(position), 0);
            return webViewList.get(position);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }
    }

}

