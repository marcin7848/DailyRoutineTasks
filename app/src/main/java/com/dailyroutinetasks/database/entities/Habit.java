package com.dailyroutinetasks.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Habit {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "start_time")
    private Calendar startTime;

    @ColumnInfo(name = "duration_hours")
    private Integer durationHours;

    @ColumnInfo(name = "duration_minutes")
    private Integer durationMinutes;

    @ColumnInfo(name = "order_number")
    private Integer orderNumber;

    @ColumnInfo(name = "active")
    private Boolean active;

    @ColumnInfo(name = "days_of_week")
    private Integer daysOfWeek;
}