package com.dailyroutinetasks.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


import com.dailyroutinetasks.database.entities.Day;

import java.util.List;

@Dao
public interface DayDao {
    @Query("SELECT * FROM day")
    List<Day> getAll();

    @Query("SELECT * FROM day WHERE id IN (:ids)")
    List<Day> loadAllByIds(int[] ids);

    @Query("SELECT * FROM day WHERE day_string = :dayString")
    Day getDayByDayString(String dayString);

    @Query("SELECT * FROM day WHERE id = :dayId")
    Day getDayById(long dayId);

    @Insert
    long insert(Day day);


}