package com.example.avi;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class Notifications {
    
    //Sends a notificaiton.
   @RequiresApi(api = Build.VERSION_CODES.O)
   public void notification(String title, String text, int notificationId, Context context){

       String channel_id = "The channel ID";
       CharSequence channel_name = "The channel name";
       NotificationChannel notificationChannel = new NotificationChannel(channel_id , channel_name, NotificationManager.IMPORTANCE_HIGH);
       NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
       notificationManager.createNotificationChannel(notificationChannel);


       NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id);
               builder.setSmallIcon(R.mipmap.ic_launcher);
               builder.setContentTitle(title);
               builder.setContentText(text);
               builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

      notificationManager.notify(notificationId, builder.build());

   }

}
