package com.dailyroutinetasks.database.dao;

import androidx.room.Dao;
import androidx.room.Query;


import com.dailyroutinetasks.database.entities.Day;

import java.util.List;

@Dao
public interface DayDao {
    @Query("SELECT * FROM day")
    List<Day> getAll();

    @Query("SELECT * FROM day WHERE id IN (:ids)")
    List<Day> loadAllByIds(int[] ids);


}