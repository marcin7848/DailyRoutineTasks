package com.dailyroutinetasks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.dailyroutinetasks.database.AppDatabase;
import com.dailyroutinetasks.database.entities.Setting;

import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "dailyRoutineTasksDb").build();

        AsyncTask.execute(() -> {
            updateDays(db);
        });

        Spinner spinnerDay = (Spinner) findViewById(R.id.spinnerDay);
        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_menu, menu);
        this.fillSpinner();
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

    public void fillSpinner(){
        String[] arraySpinner = new String[] {
                "test1", "test2", "test3", "test4", "test5", "test6", "test7"
        };
        Spinner s = (Spinner) findViewById(R.id.spinnerDay);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.style_spinner_day_element, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        int spinnerPosition = adapter.getPosition("test4");
        s.setSelection(spinnerPosition);
    }

    public void updateDays(AppDatabase db){
        Setting setting = db.settingDao().findByConfigName("lastDay");
        if(setting == null){
            setting = new Setting("lastDay", "0");
            db.settingDao().insertSetting(setting);
        }
        Log.d("settingId", "" + setting.getId());


        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        Long time = calendar.getTimeInMillis();
        Log.d("timeZone", calendar.getTimeZone().toString());

        Log.d("hourCalendar", ""+ calendar.get(Calendar.HOUR_OF_DAY));

    }
}