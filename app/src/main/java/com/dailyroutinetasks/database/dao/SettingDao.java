package com.dailyroutinetasks.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.dailyroutinetasks.database.entities.Setting;

import java.util.List;

@Dao
public interface SettingDao {
    @Query("SELECT * FROM Setting")
    List<Setting> getAll();

    @Query("SELECT * FROM Setting WHERE id IN (:ids)")
    List<Setting> loadAllByIds(int[] ids);

    @Query("SELECT * FROM Setting WHERE config_name LIKE :configName LIMIT 1")
    Setting findByConfigName(String configName);

    @Insert
    void insertSetting(Setting setting);

}