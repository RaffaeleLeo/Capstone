package com.example.avi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class AlarmNotification extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Notifications notifier = new Notifications();
        notifier.notification(intent.getStringExtra("title"),intent.getStringExtra("text"), (int) System.currentTimeMillis(),context);
    }
}