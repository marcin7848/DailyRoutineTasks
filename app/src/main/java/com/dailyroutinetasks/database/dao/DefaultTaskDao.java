package com.dailyroutinetasks.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.dailyroutinetasks.database.entities.DefaultTask;

import java.util.List;

@Dao
public interface DefaultTaskDao extends TaskGenericDao<DefaultTask>{

    @Query("SELECT * FROM defaulttask ORDER BY position_number ASC")
    List<DefaultTask> getAll();

    @Query("SELECT * FROM defaulttask WHERE id IN (:ids)")
    List<DefaultTask> loadAllByIds(int[] ids);

    @Query("SELECT * FROM defaulttask ORDER BY position_number DESC LIMIT 1")
    DefaultTask getDefaultTaskWithLastPositionNumber();

}