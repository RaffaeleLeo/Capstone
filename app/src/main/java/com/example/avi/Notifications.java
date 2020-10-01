package com.example.avi;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

public class Notifications extends Activity {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


   public void notification(String title, String text){
       NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
               builder.setSmallIcon();
               builder.setContentTitle(title);
               builder.setContentText(text);
               builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

       NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

       // notificationId is a unique int for each notification that you must define
       int notificationId = 1;
       notificationManager.notify(notificationId, builder.build());

   }

}
