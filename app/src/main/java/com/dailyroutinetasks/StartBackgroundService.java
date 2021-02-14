package com.dailyroutinetasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

public class StartBackgroundService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(!BackgroundWorker.isServiceStarted) {
            BackgroundWorker.isServiceStarted = true;
            WorkRequest backgroundWorker =
                    new OneTimeWorkRequest.Builder(BackgroundWorker.class)
                            .build();
            WorkManager
                    .getInstance(context)
                    .enqueue(backgroundWorker);
        }
    }
}