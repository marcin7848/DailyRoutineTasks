package com.dailyroutinetasks.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.appcompat.app.AppCompatDelegate;

import com.dailyroutinetasks.MainActivity;
import com.dailyroutinetasks.R;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class DailyRoutineTasksWidget extends AppWidgetProvider {

    public static void sendRefreshBroadcast(Context context) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(context, DailyRoutineTasksWidget.class));
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            // refresh all your widgets
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, DailyRoutineTasksWidget.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.widgetTasksList);
        }
        super.onReceive(context, intent);
    }


    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.daily_routine_tasks_widget);
        remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        Intent intentService = new Intent(context, TaskWidgetRemoteViewsService.class);
        remoteViews.setRemoteAdapter(R.id.widgetTasksList, intentService);

        Calendar currentDay = Calendar.getInstance(TimeZone.getDefault());
        String dayText = currentDay.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) + " " +
               currentDay.get(Calendar.DAY_OF_MONTH) + " " +
               currentDay.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " +
               currentDay.get(Calendar.YEAR);

        remoteViews.setTextViewText(R.id.widget_task_day, dayText);



        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them


        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

}