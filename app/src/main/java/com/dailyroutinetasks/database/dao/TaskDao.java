package com.dailyroutinetasks.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.dailyroutinetasks.database.entities.Day;
import com.dailyroutinetasks.database.entities.DefaultTask;
import com.dailyroutinetasks.database.entities.Task;

import java.util.List;

@Dao
public interface TaskDao extends GenericDao<Task> {
    @Query("SELECT * FROM task")
    List<Task> getAll();

    @Query("SELECT * FROM Task WHERE id IN (:ids)")
    List<Task> loadAllByIds(int[] ids);

    @Query("SELECT * FROM task where day_id = :dayId ORDER BY position_number ASC")
    List<Task> getTasksByDayId(Long dayId);

    @Query("SELECT * FROM Task WHERE done = 0 AND id NOT IN (:exceptTasksIds)  ORDER BY start_time ASC LIMIT 1")
    Task getNearestTask(List<Long> exceptTasksIds);

}