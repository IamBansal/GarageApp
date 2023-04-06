package com.example.garageapp.repository

import com.example.garageapp.api.RetrofitInstance
import com.example.garageapp.db.CarDatabase
import com.example.garageapp.model.Result

class CarRepository(
   private val database: CarDatabase
) {

   suspend fun getAllMakes() = RetrofitInstance.api.getAllMakes()

   suspend fun getAllModel(makeId: Int) = RetrofitInstance.api.getAllModels(makeId)

   suspend fun upsert(car: Result) = database.getArticleDao().upsert(car)

   suspend fun delete(car: Result) = database.getArticleDao().delete(car)

   fun getAllCars() = database.getArticleDao().getAllCars()
}