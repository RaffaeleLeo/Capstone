package com.example.avi;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class PullDangerData extends AsyncTask<String, Integer, ArrayList<String>>{

    @Override
    protected ArrayList<String> doInBackground(String... strings) {
        ArrayList<String> result = new ArrayList<>();

        URL url = null;
        System.setProperty("http.agent", "");
        try {
            url = new URL("https://utahavalanchecenter.org/forecast/salt-lake/json\r\n");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "nathan.zaltsman@gmail.com");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            InputStream in = urlConnection.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String text = r.readLine();
            String lines[]= text.split(";");
            String data = lines[lines.length - 1];
            String danger[] = data.split("\"");
            String vals = danger[16];
            String vals_split[] = vals.split(",");

            //Add each danger rating for each compass spot at 0-23
            for(int i = 0; i < vals_split.length; i++){
                result.add(vals_split[i]);
            }

            //Add the image url at 24
            result.add(danger[20]);

            //Add the overall danger at 25
            result.add(danger[24]);

            //Add the region at 26
            result.add(danger[28]);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        return result;
    }
}
