package com.dailyroutinetasks.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.dailyroutinetasks.database.dao.DayDao;
import com.dailyroutinetasks.database.dao.DefaultTaskDao;
import com.dailyroutinetasks.database.dao.SettingDao;
import com.dailyroutinetasks.database.dao.TaskDao;
import com.dailyroutinetasks.database.entities.Day;
import com.dailyroutinetasks.database.entities.DefaultTask;
import com.dailyroutinetasks.database.entities.Setting;
import com.dailyroutinetasks.database.entities.Task;

@Database(entities = {Day.class, Task.class, Setting.class, DefaultTask.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract DayDao dayDao();
    public abstract TaskDao taskDao();
    public abstract SettingDao settingDao();
    public abstract DefaultTaskDao defaultTaskDao();
}
