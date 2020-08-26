package com.example.avi;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WeatherData extends AsyncTask<String, Integer, List> {

    public WeatherData() {
    }

    @Override
    protected List doInBackground(String... strings) {
        return getWeatherData();
    }


    @Override
    protected void onPostExecute(List result) {
        super.onPostExecute(result);
    }

    //Gets the current alta guard forecast
    public List getWeatherData() {
        int temp = 0;
        int dewPoint = 0;
        int relativeHumidity = 0;
        String windDirection = null;
        int windSpeed = 0;
        int windChill = 0;
        float accumulatedPrecip = 0;
        float snowInteval = 0;
        List<Weather> currentWeatherData = new ArrayList<>();
        try {
            URL myURL = new URL("https://www.wrh.noaa.gov/mesowest/getobextXml.php?sid=agd&num=72");
            HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "AviWeatherApp");
            connection.setUseCaches(false);


            if (connection.getResponseCode() == 200) {

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String line;
                //Loops through the XML response and builds a weather object
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("<var")) {
                        String[] descrVal = getWeatherItem(line);
                        switch (descrVal[0]) {
                            case "Temp":
                                temp = Integer.parseInt(descrVal[1]);
                                break;
                            case "Dewp":
                                dewPoint = Integer.parseInt(descrVal[1]);
                                break;
                            case "Relh":
                                relativeHumidity = Integer.parseInt(descrVal[1]);
                                break;
                            case "Wind Card":
                                windDirection = descrVal[1];
                                break;
                            case "Wind":
                                windSpeed = Integer.parseInt(descrVal[1]);
                                break;
                            case "Wind Chill":
                                windChill = Integer.parseInt(descrVal[1]);
                                break;
                            case "Acc Precip":
                                accumulatedPrecip = Float.parseFloat(descrVal[1]);
                                break;
                            case "Snow Intevral":
                                snowInteval = Integer.parseInt(descrVal[1]);
                            default:
                                break;
                        }
                    } else if (line.startsWith("</ob")) {
                        currentWeatherData.add(new Weather(temp, dewPoint, relativeHumidity, windDirection, windSpeed, windChill, accumulatedPrecip, snowInteval));
                    }
                }
                in.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentWeatherData;

    }

    //Returns the weather item and its value
    public String[] getWeatherItem(String line) {
        String[] tokens = line.split("=");
        String[] descVal = new String[2];
        String description = tokens[2];
        String value = tokens[4];
        descVal[0] = description.substring(1, description.length() - 6);
        descVal[1] = value.substring(1, value.length() - 3);
        return descVal;
    }

    public static class Weather {
        final int temp;
        final int dewPoint;
        final int relativeHumidity;
        final String windDirection;
        final int windSpeed;
        final int windChill;
        final float accumulatedPrecip;
        final float snowInteval;

        private Weather(int temp, int dewPoint, int relativeHumidity, String windDirection, int windSpeed,
                        int windChill, float accumulatedPrecip, float snowInteval) {
            this.temp = temp;
            this.dewPoint = dewPoint;
            this.relativeHumidity = relativeHumidity;
            this.windDirection = windDirection;
            this.windSpeed = windSpeed;
            this.windChill = windChill;
            this.accumulatedPrecip = accumulatedPrecip;
            this.snowInteval = snowInteval;
        }

        public int getTemp() {
            return temp;
        }

        public int getDewPoint() {
            return dewPoint;
        }

        public int getRelativeHumidity() {
            return relativeHumidity;
        }

        public String getWindDirection() {
            return windDirection;
        }

        public int getWindSpeed() {
            return windSpeed;
        }

        public int getWindChill() {
            return windChill;
        }

        public float getAccumulatedPrecip() {
            return accumulatedPrecip;
        }

        public float getSnowInteval() {
            return snowInteval;
        }

        @Override
        public String toString() {
            String text = String.format(
                    "Temperature: %d\n" +
                            "Dew Point: %d\n" +
                            "Relative Humidity: %d\n" +
                            "Wind Direction: %s\n" +
                            "Wind Speed: %d\n" +
                            "Wind Chill: %d\n" +
                            "Accumulated Precip: %.2f\n" +
                            "Snow Interval: %.2f",temp,dewPoint,relativeHumidity,windDirection,windSpeed,
                    windChill,accumulatedPrecip,snowInteval);
            return text;
        }

    }
}
