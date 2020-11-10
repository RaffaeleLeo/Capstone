package com.example.avi;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import com.example.avi.Journals.JournalActivity;
import com.google.android.material.tabs.TabLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LiveUpdates extends Activity {

    private ViewPager viewPager;
    private MyPagerAdapter adapter;
    private List<View> webViewList;
    Button settings;

    @Override
    protected void onResume(){
        super.onResume();
        TabLayout tabLayout = findViewById(R.id.TabLayout);
        tabLayout.getTabAt(0).select();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_updates);
        setupTabLayout();
        settings = findViewById(R.id.topBar).findViewById(R.id.settingsButton);
        TextView title = (TextView) findViewById(R.id.topBar).findViewById(R.id.pageTitle);
        title.setText("Home\nWeather Info");

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LiveUpdates.this, SettingsActivity.class);
                intent.putExtra("PRIOR", 0);
                startActivity(intent);
            }
        });



        adapter = new MyPagerAdapter();
        viewPager = findViewById(R.id.pager);

        webViewList = new ArrayList<View>();
        try {
            addWebView(webViewList, "https://utahavalanchecenter.org/forecast/salt-lake");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            addWebView(webViewList, "https://cottonwoodcanyons.udot.utah.gov/canyon-road-information/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            addWebView(webViewList, "https://utahavalanchecenter.org/avalanches");
        } catch (IOException e) {
            e.printStackTrace();
        }


        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled ( int position, float positionOffset,
            int positionOffsetPixels){

            }

            @Override
            public void onPageSelected ( int position){
                if(position == 0)
                {
                    TextView title = (TextView) findViewById(R.id.topBar).findViewById(R.id.pageTitle);
                    title.setText("Home\nWeather Info");
                }
                if(position == 1)
                {
                    TextView title = (TextView) findViewById(R.id.topBar).findViewById(R.id.pageTitle);
                    title.setText("Home\nTraffic Info");
                }
                if(position == 2)
                {
                    TextView title = (TextView) findViewById(R.id.topBar).findViewById(R.id.pageTitle);
                    title.setText("Home\nAvalanche Info");
                }
            }

            @Override
            public void onPageScrollStateChanged ( int state){

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addWebView(List<View> viewList, String url) throws IOException {
        WebView webView=new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);


        String webpage = "";

        if ( (url == "https://cottonwoodcanyons.udot.utah.gov/canyon-road-information/") || ( (System.currentTimeMillis() -
                this.getApplicationContext().getSharedPreferences("Prefs", 0).getLong( url + "when?",  0)) > 86400000)
                || (this.getApplicationContext().getSharedPreferences("Prefs", 0).getLong( url + "when?",  0) <
                new Date( Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), 8, 0).getSeconds() * 1000 )) {
            WeatherData dataGetter = new WeatherData();
            try {
                webpage = dataGetter.execute(url).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            webView.loadDataWithBaseURL(url, webpage,"text/html", "utf-8", null);

            viewList.add(webView);

            SharedPreferences prefs = getApplicationContext().getSharedPreferences("Prefs", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(url, webpage);
            editor.putLong(url + "when?", System.currentTimeMillis());
            editor.commit();
        }
        else
        {
            webView.loadDataWithBaseURL(url, this.getApplicationContext().getSharedPreferences("Prefs", 0).getString(url, ""),"text/html", "utf-8", null);
            viewList.add(webView);
        }
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

                if (tab.getPosition() == 1) {
                    Intent intent = new Intent(LiveUpdates.this, MapsActivity.class);

                    startActivity(intent);
                } else if (tab.getPosition() == 3) {
                    //Intent intent = new Intent(LiveUpdates.this, ChatRoomActivity.class);
                    Intent intent = new Intent(LiveUpdates.this, SocialMediaHomeActivity.class);

                    startActivity(intent);
                } else if (tab.getPosition() == 2) {
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

