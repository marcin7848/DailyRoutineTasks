package com.dailyroutinetasks.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Calendar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static androidx.room.ForeignKey.CASCADE;

@NoArgsConstructor
@Getter
@Setter
@Entity(foreignKeys = @ForeignKey(entity = Day.class,
        parentColumns = "id",
        childColumns = "day_id",
        onDelete = CASCADE))
public class Task {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "start_time", index = true)
    private Calendar startTime;

    @ColumnInfo(name = "duration_hours")
    private Integer durationHours;

    @ColumnInfo(name = "duration_minutes")
    private Integer durationMinutes;

    @ColumnInfo(name = "position_number", index = true)
    private Integer positionNumber;

    @ColumnInfo(name = "day_id", index = true)
    private Long dayId;

}