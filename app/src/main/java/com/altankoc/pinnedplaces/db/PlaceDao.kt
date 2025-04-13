package com.altankoc.pinnedplaces.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.altankoc.pinnedplaces.model.Place


@Dao
interface PlaceDao {

    @Query("Select * from places")
    suspend fun getAll(): List<Place>

    @Insert
    suspend fun insert(place: Place)

    @Delete
    suspend fun delete(place: Place)
}