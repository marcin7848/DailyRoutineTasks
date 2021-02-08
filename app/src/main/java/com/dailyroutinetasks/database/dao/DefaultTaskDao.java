package com.dailyroutinetasks.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.dailyroutinetasks.database.entities.DefaultTask;

import java.util.List;

@Dao
public interface DefaultTaskDao {
    @Query("SELECT * FROM defaulttask")
    List<DefaultTask> getAll();

    @Query("SELECT * FROM defaulttask WHERE id IN (:ids)")
    List<DefaultTask> loadAllByIds(int[] ids);

    @Insert
    long insert(DefaultTask defaultTask);

    @Delete
    void delete(DefaultTask defaultTask);

    @Update
    void update(DefaultTask defaultTask);
}