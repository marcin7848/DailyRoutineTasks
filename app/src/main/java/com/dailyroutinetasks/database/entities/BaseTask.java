package com.dailyroutinetasks.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
abstract public class BaseTask {

    @Ignore
    protected BaseTask(){}

    @Ignore
    protected BaseTask(String title, Integer durationHours, Integer durationMinutes, Integer positionNumber) {
        this.title = title;
        this.durationHours = durationHours;
        this.durationMinutes = durationMinutes;
        this.positionNumber = positionNumber;
    }

    @ColumnInfo(name = "title")
    protected String title;

    @ColumnInfo(name = "duration_hours")
    protected Integer durationHours;

    @ColumnInfo(name = "duration_minutes")
    protected Integer durationMinutes;

    @ColumnInfo(name = "position_number", index = true)
    protected Integer positionNumber;
}
