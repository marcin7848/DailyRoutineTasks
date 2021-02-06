package com.dailyroutinetasks.database;

import androidx.room.TypeConverter;

import java.util.Calendar;

public class Converters {
    @TypeConverter
    public static Calendar toCalendar(Long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        return c;
    }

    @TypeConverter
    public static Long fromCalendar(Calendar c){
        return c == null ? null : c.getTime().getTime();
    }
}