package com.example.garageapp.api

import com.example.garageapp.model.CarMakeResponse
import com.example.garageapp.utils.Constants.Companion.MAKE_ENDPOINT
import com.example.garageapp.utils.Constants.Companion.MODEL_ENDPOINT
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CarApi {

    @GET(MAKE_ENDPOINT)
    suspend fun getAllMakes() : Response<CarMakeResponse>

    @GET(MODEL_ENDPOINT)
    suspend fun getAllModels(
        @Path(value = "makeId") makeId: Int,
    ) : Response<CarMakeResponse>

}