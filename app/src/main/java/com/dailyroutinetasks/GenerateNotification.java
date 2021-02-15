package com.dailyroutinetasks;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class GenerateNotification extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //run this broadcast receiver after reboot // if not already is running (ale chyba nie muszę sprawdzać, bo jak usunę wszystkie alarmy to nie odpali się ponownie ten obecnie działający)
        createNotificationChannel(context);

        //clear all pending notifications
        //load data from db and set alarm
        //remember to add sending broadcast to this while deleting/editing/adding/moving tasks

        Intent notificationIntent = new Intent(context, DisplayNotification.class);
        notificationIntent.putExtra("title", "TYtyulek");
        notificationIntent.putExtra("text", "Contnencik");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long timeAtGet = System.currentTimeMillis();

        long tenSeconds = timeAtGet + 10 * 1000;

        alarmManager.set(AlarmManager.RTC_WAKEUP,
                tenSeconds,
                pendingIntent);

    }

    private void createNotificationChannel(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "DailyRoutineTasksChannel";
            String description = "Channel for Daily Routine Tasks";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel("dailyRoutineTaskNotification", name, importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
