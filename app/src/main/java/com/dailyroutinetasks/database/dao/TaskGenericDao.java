package com.dailyroutinetasks.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

@Dao
public  interface TaskGenericDao<T>{

    @Insert
     long insert(T t);

    @Delete
    void delete(T t);

    @Update
    void update(T t);
}