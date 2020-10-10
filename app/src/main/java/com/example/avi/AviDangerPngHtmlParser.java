package com.example.avi;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.toolbox.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class AviDangerPngHtmlParser extends AsyncTask<String, Integer, Bitmap> {


    public AviDangerPngHtmlParser() {
    }

    public void getAviIcon() throws IOException {

    }

    private void readStream(InputStream in) {

    }

    @Override
    protected Bitmap doInBackground(String... strings) {

        URL url = null;
        Bitmap bitmap = null;
        String aviDangerCompassURL = "https://utahavalanchecenter.org/";
        ArrayList<String> aviDangerPngUrls = new ArrayList<>();
        try {
            url = new URL("https://utahavalanchecenter.org/forecast/salt-lake");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            InputStream in = urlConnection.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                if (line.contains("/sites/default/files/forecast/")) {
                    line = line.split("src=\"")[1];
                    aviDangerPngUrls.add(line.substring(0, line.length() - 2));

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        aviDangerCompassURL = (aviDangerCompassURL + aviDangerPngUrls.get(0)).replaceAll("\\s+", "");
        try {
            url = new URL(aviDangerCompassURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            InputStream in = urlConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(in);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
