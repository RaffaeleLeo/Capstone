package com.example.avi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import android.

public class WeatherData {

    public WeatherData(String lat, String lon){
        URL url;
        HttpURLConnection conn;
        HttpClient client;
        try{
            url = new URL("api.openweathermap.org");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            DataOutputStream wr = new DataOutputStream (
                    conn.getOutputStream());
            wr.writeBytes("/data/2.5/weather?lat={" + lat + "}&lon={"+ lon +"}&appid={9341d1b894727a76de3b79ee66011e5a}");
            wr.close();
            InputStream is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            System.out.println(response.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
