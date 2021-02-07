package com.dailyroutinetasks.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
@Entity
public class DefaultTask {

    @Ignore
    public DefaultTask(String title, Integer durationHours, Integer durationMinutes, Integer orderNumber) {
        this.title = title;
        this.durationHours = durationHours;
        this.durationMinutes = durationMinutes;
        this.orderNumber = orderNumber;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "duration_hours")
    private Integer durationHours;

    @ColumnInfo(name = "duration_minutes")
    private Integer durationMinutes;

    @ColumnInfo(name = "order_number")
    private Integer orderNumber;

}