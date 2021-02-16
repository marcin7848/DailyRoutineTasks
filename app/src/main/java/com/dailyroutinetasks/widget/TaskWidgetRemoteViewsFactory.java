package com.dailyroutinetasks.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.room.Room;

import com.dailyroutinetasks.GlobalFunctions;
import com.dailyroutinetasks.R;
import com.dailyroutinetasks.database.AppDatabase;
import com.dailyroutinetasks.database.entities.Day;
import com.dailyroutinetasks.database.entities.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class TaskWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory  {

    private Context mContext;
    AppDatabase db;
    List<Task> tasks = new ArrayList<>();

    public TaskWidgetRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
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

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.task_row_widget);
        rv.setTextViewText(R.id.widget_task_row_title, tasks.get(position).getTitle());

        return rv;
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
