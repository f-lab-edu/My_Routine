package com.example.myroutine.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myroutine.data.local.entity.HolidayCacheMetadata

@Dao
interface HolidayCacheMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: HolidayCacheMetadata)

    @Query("SELECT * FROM holiday_cache_metadata WHERE year = :year AND month = :month")
    suspend fun getMetadataByMonth(year: Int, month: Int): HolidayCacheMetadata?

    @Query("DELETE FROM holiday_cache_metadata WHERE year = :year AND month = :month")
    suspend fun deleteMetadataByMonth(year: Int, month: Int)

    @Query("DELETE FROM holiday_cache_metadata")
    suspend fun deleteAll()
}
