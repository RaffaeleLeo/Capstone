package com.example.avi;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ElevationData extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... strings) {
        return getElevation(strings);
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    private String getElevation(String[] latLong){
        String elevation = "";
        try {
            URL myURL = new URL("https://nationalmap.gov/epqs/pqs.php?y="+ latLong[0]+ "&x=" + latLong[1] +"&output=json&units=feet");
            HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();

            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                    Log.i("Response", "got to resp");
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            connection.getInputStream()));
                    String line;
                    //Loops through the XML response and builds a weather object
                    while ((line = in.readLine()) != null) {
                        JSONObject jsonObj = new JSONObject(line);
                        elevation = Double.toString(jsonObj.getJSONObject("USGS_Elevation_Point_Query_Service").getJSONObject("Elevation_Query").getDouble("Elevation"));
                    }
                    in.close();
            }
        } catch (Exception e) {
            Log.e("Err", e.toString());
        }
        Log.d("Ele", elevation);
        return elevation;
    }
}
