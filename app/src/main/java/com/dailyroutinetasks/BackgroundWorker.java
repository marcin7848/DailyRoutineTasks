package com.dailyroutinetasks;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

public class BackgroundWorker extends Worker {

    static boolean isServiceStarted = false;
    Context context;

    public BackgroundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        backgroundProcess();
        return Result.success();
    }


    public void backgroundProcess(){
        MediaPlayer player = MediaPlayer.create(context, Settings.System.DEFAULT_ALARM_ALERT_URI);
        player.setLooping(true);
        player.start();
        Log.d("test", "poszlo");
        SystemClock.sleep(4000);
        player.stop();
        backgroundProcess();
    }

    @Override
    public void onStopped() {
        BackgroundWorker.isServiceStarted = false;
        Intent backgroundWorker = new Intent(getApplicationContext(), StartBackgroundService.class);
        context.sendBroadcast(backgroundWorker);
        super.onStopped();
    }

    /*
    static void enqueueWork(Context context, Intent intent){
        enqueueWork(context, BackgroundService.class, 123, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        BackgroundService.isServiceStarted = true;

        player = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
        player.setLooping(true);
        player.start();
    }

*/
    /*    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BackgroundService.isServiceStarted = true;

        player = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
        player.setLooping(true);
        player.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BackgroundService.isServiceStarted = false;
        Intent intent = new Intent(getApplicationContext(), this.getClass());
        sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        BackgroundService.isServiceStarted = false;
        Intent intent = new Intent(getApplicationContext(), this.getClass());
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }*/


}