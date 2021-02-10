package com.dailyroutinetasks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import com.dailyroutinetasks.database.AppDatabase;
import com.dailyroutinetasks.database.entities.Setting;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    AppDatabase db;
    Calendar today;
    Calendar pickedDay;
    Dialog pickDayDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "dailyRoutineTasksDb").build();

        today = Calendar.getInstance(TimeZone.getDefault());
        pickedDay = (Calendar) today.clone();

        updateTasksView();

        pickDayDialog = new Dialog(this);
        pickDayDialog.setContentView(R.layout.pick_day_view);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.defaultTasks:
                Intent intentDefaultTasks = new Intent(this, DefaultTasksActivity.class);
                startActivity(intentDefaultTasks);
                return true;
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public void updateDays(AppDatabase db){
        Setting setting = db.settingDao().findByConfigName("lastDay");
        if(setting == null){
            setting = new Setting("lastDay", "0");
            db.settingDao().insert(setting);
        }
        Log.d("settingId", "" + setting.getId());


        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        Long time = calendar.getTimeInMillis();
        Log.d("timeZone", calendar.getTimeZone().toString());

        Log.d("hourCalendar", Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));

    }

    private void updateTasksView(){
        TextView task_day = findViewById(R.id.task_day);
        task_day.setText(pickedDay.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) + " " +
                pickedDay.get(Calendar.DAY_OF_MONTH) + " " +
                pickedDay.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " +
                pickedDay.get(Calendar.YEAR));
    }

    public void showPickDayDialog(View v){
        pickDayDialog.show();
        CalendarView pickDayCalendar = pickDayDialog.findViewById(R.id.pickDayCalendarView);
        pickDayCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                pickedDay.set(year, month, dayOfMonth);
                updateTasksView();
                pickDayDialog.hide();
            }
        });
    }
}