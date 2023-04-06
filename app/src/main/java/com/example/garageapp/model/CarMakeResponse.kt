package com.example.garageapp.model

import com.example.garageapp.model.Result

data class CarMakeResponse(
    val Count: Int,
    val Message: String,
    val Results: List<Result>,
    val SearchCriteria: Any
) : java.io.Serializable