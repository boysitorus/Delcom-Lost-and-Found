package com.ifs21025.lostandfound.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ifs21025.lostandfound.data.local.entity.DelcomLostFoundEntity

@Dao
interface IDelcomLostFoundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(delcomLostFound: DelcomLostFoundEntity)

    @Delete
    fun delete(delcomLostFound: DelcomLostFoundEntity)

    @Query("SELECT * FROM delcom_lost_founds WHERE id = :id LIMIT 1")
    fun get(id: Int): LiveData<DelcomLostFoundEntity?>

    @Query("SELECT * FROM delcom_lost_founds ORDER BY created_at DESC")
    fun getAllLostFounds(): LiveData<List<DelcomLostFoundEntity>?>
} 