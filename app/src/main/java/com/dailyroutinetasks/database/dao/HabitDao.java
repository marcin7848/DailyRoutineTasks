package com.dailyroutinetasks.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.dailyroutinetasks.database.entities.Habit;

import java.util.List;

@Dao
public interface HabitDao {
    @Query("SELECT * FROM habit")
    List<Habit> getAll();

    @Query("SELECT * FROM habit WHERE id IN (:ids)")
    List<Habit> loadAllByIds(int[] ids);


}