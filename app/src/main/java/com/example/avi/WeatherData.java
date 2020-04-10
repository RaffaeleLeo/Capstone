package com.example.avi;

import com.google.android.gms.common.util.IOUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;


public class WeatherData {

    public WeatherData(String lat, String lon){
        try{
            URL myURL = new URL("https://www.wrh.noaa.gov/mesowest/getobextXml.php?sid=agd&num=72");
            HttpURLConnection connection = (HttpURLConnection)myURL.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent","AviWeatherApp");
            connection.setUseCaches(false);

            int responseCode = connection.getResponseCode();
            //String responseMsg = connection.getResponseMessage();

            if (responseCode == 200) {
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // print result
                    System.out.println(response.toString());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
