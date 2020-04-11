package com.example.avi;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void WeatherDataTest(){
        //String.valueOf(40.628178), String.valueOf(-111.623462) mountain test coordinates
        WeatherData data = new WeatherData();
        //Gets the most recent weather data
        WeatherData.Weather weather = (WeatherData.Weather) data.getWeatherData().get(0);
        System.out.println(weather.getWindDirection());


    }
}