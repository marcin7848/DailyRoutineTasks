package com.dailyroutinetasks.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Calendar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static androidx.room.ForeignKey.CASCADE;
import static androidx.room.ForeignKey.NO_ACTION;

@NoArgsConstructor
@Getter
@Setter
@Entity(foreignKeys = @ForeignKey(entity = Day.class,
        parentColumns = "id",
        childColumns = "day_id",
        onDelete = CASCADE), inheritSuperIndices = true)
public class Task extends BaseTask{

    @Ignore
    public Task(Long id, String title, Integer durationHours, Integer durationMinutes, Integer positionNumber, boolean done, Calendar startTime, Long dayId) {
        super(title, durationHours, durationMinutes, positionNumber);
        this.id = id;
        this.done = done;
        this.startTime = startTime;
        this.dayId = dayId;
    }

    @Ignore
    public Task(String title, Integer durationHours, Integer durationMinutes, Integer positionNumber, boolean done, Calendar startTime, Long dayId) {
        super(title, durationHours, durationMinutes, positionNumber);
        this.done = done;
        this.startTime = startTime;
        this.dayId = dayId;
    }

    @Ignore
    public Task(Task task) {
        super(task.title, task.durationHours, task.durationMinutes, task.positionNumber);
        this.id = task.id;
        this.done = task.done;
        this.startTime = (Calendar) task.startTime.clone();
        this.dayId = task.dayId;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "done", index = true)
    private boolean done;

    @ColumnInfo(name = "start_time", index = true)
    private Calendar startTime;

    @ColumnInfo(name = "day_id", index = true)
    private Long dayId;

}