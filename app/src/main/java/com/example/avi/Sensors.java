package com.example.avi;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import static androidx.core.content.ContextCompat.getSystemService;

//https://www.deviantdev.com/journal/android-compass-azimuth-calculating

public class Sensors extends Activity implements SensorEventListener {

    public int compassAngleDegree = 0;
    SensorManager manageSensors;
    Sensor compass;
    Sensor accelerometer;
    float compassStateX;
    float compassStateY;
    float compassStateZ;
    float AccelerometerStateX;
    float AccelerometerStateY;
    float AccelerometerStateZ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manageSensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        compass = manageSensors.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        accelerometer = manageSensors.getDefaultSensor(Sensor.TYPE_GRAVITY);

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR){
            compassStateX = event.values[0];
            compassStateY = event.values[1];
            compassStateZ = event.values[2];
        }
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
            AccelerometerStateX = event.values[0];
            AccelerometerStateY = event.values[1];
            AccelerometerStateZ = event.values[2];
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        manageSensors.registerListener(this, manageSensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        manageSensors.registerListener(this, manageSensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manageSensors.unregisterListener(this);
    }
}
