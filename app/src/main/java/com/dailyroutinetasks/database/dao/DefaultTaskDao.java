package com.dailyroutinetasks.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.dailyroutinetasks.database.entities.DefaultTask;

import java.util.List;

@Dao
public interface DefaultTaskDao extends GenericDao<DefaultTask> {

    @Query("SELECT * FROM defaulttask ORDER BY position_number ASC")
    List<DefaultTask> getAll();

    @Query("SELECT * FROM defaulttask WHERE id IN (:ids)")
    List<DefaultTask> loadAllByIds(int[] ids);

    @Query("SELECT * FROM defaulttask ORDER BY position_number DESC LIMIT 1")
    DefaultTask getDefaultTaskWithLastPositionNumber();

}