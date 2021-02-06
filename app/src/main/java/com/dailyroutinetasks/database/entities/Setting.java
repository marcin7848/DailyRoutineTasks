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
public class Setting {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Long id;

    @ColumnInfo(name = "config_name")
    private String configName;

    @ColumnInfo(name = "config_value")
    private String configValue;

    @Ignore
    public Setting(String configName, String configValue){
        this.configName = configName;
        this.configValue = configValue;
    }
}