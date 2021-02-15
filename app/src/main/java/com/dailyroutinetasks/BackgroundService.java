package com.dailyroutinetasks;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.Settings;

public class BackgroundService extends Service {

    static boolean isServiceStarted = false;
    MediaPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BackgroundService.isServiceStarted = true;

/*
        Intent notificationIntent = new Intent(this, BackgroundService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, "dailyroutinetasksforeground")
                        .setContentTitle("title")
                        .setContentText("content")
                        .setSmallIcon(R.drawable.ic_foreground)
                        .setContentIntent(pendingIntent)
                        .setTicker("ticker")
                        .build();

        startForeground(1324, notification);
*/

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
    }


}