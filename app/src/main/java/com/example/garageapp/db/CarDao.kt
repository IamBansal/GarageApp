package com.example.garageapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.garageapp.model.Result

@Dao
interface CarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(car: Result) : Long

    @Delete
    suspend fun delete(car: Result)

    @Query("SELECT * FROM car_result")
    fun getAllCars() : LiveData<List<Result>>
}