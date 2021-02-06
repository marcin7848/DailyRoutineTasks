package com.dailyroutinetasks.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.dailyroutinetasks.database.entities.Task;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM task")
    List<Task> getAll();

    @Query("SELECT * FROM Task WHERE id IN (:ids)")
    List<Task> loadAllByIds(int[] ids);


}