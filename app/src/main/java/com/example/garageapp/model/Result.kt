package com.example.garageapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "car_result")
data class Result(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val Make_ID: Int,
    val Make_Name: String,
    var Model_ID: Int? = null,
    var Model_Name: String? = null,
    var imagePath: String? = null
): java.io.Serializable