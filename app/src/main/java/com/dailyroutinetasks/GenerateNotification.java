package com.dailyroutinetasks;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.dailyroutinetasks.database.AppDatabase;
import com.dailyroutinetasks.database.entities.Task;
import com.dailyroutinetasks.widget.DailyRoutineTasksWidget;

import java.util.ArrayList;
import java.util.List;

public class GenerateNotification extends BroadcastReceiver {

    AppDatabase db;
    List<Long> exceptTasksIds = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context);

        Intent notificationIntent = new Intent(context, DisplayNotification.class);

        //clear all pending notifications
        PendingIntent sender = PendingIntent.getBroadcast(context, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager3 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        sender.cancel();
        alarmManager3.cancel(sender);

        db = Room.databaseBuilder(context,
                AppDatabase.class, "dailyRoutineTasksDb").build();

        exceptTasksIds.clear();

        AsyncTask.execute(()-> {
            Task task = getTaskToNotification();

            DailyRoutineTasksWidget.sendRefreshBroadcast(context);


            if(task != null && !PreferenceManager
                    .getDefaultSharedPreferences(context).getBoolean("disable_notifications", false)) {

                notificationIntent.putExtra("title", task.getTitle());
                notificationIntent.putExtra("text", "This task has just begun!");

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, notificationIntent, 0);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        task.getStartTime().getTimeInMillis(),
                        pendingIntent);
            }
        });

    }

    private void createNotificationChannel(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "DailyRoutineTasksChannel";
            String description = "Channel for Daily Routine Tasks";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel("dailyRoutineTaskNotification", name, importance);
            notificationChannel.setDescription(description);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            long[] vibration = {500,200,200,500};
            notificationChannel.setVibrationPattern(vibration);
            notificationChannel.enableVibration(true);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(0xff00ffff);

            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationChannel.setSound(sound, att);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private Task getTaskToNotification(){
        Task task = db.taskDao().getNearestTaskExceptIds(exceptTasksIds);
        if(task == null)
            return null;

        long currentTime = System.currentTimeMillis();
        if(task.getStartTime().getTimeInMillis() < currentTime){
            long timePlusDuration = task.getStartTime().getTimeInMillis() + task.getDurationHours() * 60*60*1000 + task.getDurationMinutes() * 60*1000;
            if(timePlusDuration < currentTime){
                task.setDone(true);
                db.taskDao().update(task);
            }else{
                exceptTasksIds.add(task.getId());
            }
            return getTaskToNotification();
        }else{
            return task;
        }

    }
}
