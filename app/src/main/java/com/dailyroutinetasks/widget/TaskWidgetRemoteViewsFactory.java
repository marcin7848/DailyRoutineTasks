package com.dailyroutinetasks.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.room.Room;

import com.dailyroutinetasks.GlobalFunctions;
import com.dailyroutinetasks.MainActivity;
import com.dailyroutinetasks.R;
import com.dailyroutinetasks.database.AppDatabase;
import com.dailyroutinetasks.database.entities.Day;
import com.dailyroutinetasks.database.entities.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class TaskWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory  {

    private final Context context;
    AppDatabase db;
    List<Task> tasks = new ArrayList<>();

    public TaskWidgetRemoteViewsFactory(Context applicationContext, Intent intent) {
        context = applicationContext;
        db = Room.databaseBuilder(applicationContext,
                AppDatabase.class, "dailyRoutineTasksDb").build();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        Calendar currentDay = Calendar.getInstance(TimeZone.getDefault());
        Day existingDay = db.dayDao().getDayByDayString(GlobalFunctions.convertCalendarToDateString(currentDay));
        if(existingDay != null){
            tasks.addAll(db.taskDao().getTasksByDayId(existingDay.getId()));
            tasks.removeIf(Task::isDone);
        }
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.task_row_widget);
        remoteViews.setTextViewText(R.id.widget_task_row_title, tasks.get(position).getTitle());
        remoteViews.setTextViewText(R.id.widget_task_time_text, GlobalFunctions.convertCalendarToTimeString(tasks.get(position).getStartTime()));
        String minutes = tasks.get(position).getDurationMinutes() < 10 ? "0" + tasks.get(position).getDurationMinutes() : "" + tasks.get(position).getDurationMinutes();
        remoteViews.setTextViewText(R.id.widget_task_duration_text, String.format("%d:%sh", tasks.get(position).getDurationHours(), minutes));

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
