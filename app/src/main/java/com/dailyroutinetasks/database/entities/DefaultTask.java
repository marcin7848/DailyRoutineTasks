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
@Entity(inheritSuperIndices = true)
public class DefaultTask extends BaseTask {

    @Ignore
    public DefaultTask(Long id, String title, Integer durationHours, Integer durationMinutes, Integer positionNumber) {
        super(title, durationHours, durationMinutes, positionNumber);
        this.id = id;
    }

    @Ignore
    public DefaultTask(String title, Integer durationHours, Integer durationMinutes, Integer positionNumber) {
        super(title, durationHours, durationMinutes, positionNumber);
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

}