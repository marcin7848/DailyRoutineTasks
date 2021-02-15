package com.dailyroutinetasks;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import static android.content.Context.NOTIFICATION_SERVICE;

public class StartBackgroundService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(!BackgroundService.isServiceStarted) {
            BackgroundService.isServiceStarted = true;

            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //context.startForegroundService(new Intent(context, BackgroundService.class));
            //} else {
                //context.startService(new Intent(context, BackgroundService.class));
            //}

        }
    }
}