@file:Suppress("DEPRECATION")

package com.example.garageapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.garageapp.model.CarMakeResponse
import com.example.garageapp.repository.CarRepository
import com.example.garageapp.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import com.example.garageapp.model.Result

class CarViewModel(
    app: Application,
    private val repository: CarRepository
): AndroidViewModel(app) {

    val carMake: MutableLiveData<Resource<CarMakeResponse>> = MutableLiveData()
    val carModel: MutableLiveData<Resource<CarMakeResponse>> = MutableLiveData()

    fun getCarMake() = viewModelScope.launch {
        getSafeCarMake()
    }

    fun getCarModel(makeId: Int) = viewModelScope.launch {
        getSafeCarModel(makeId)
    }

    fun saveCar(car: Result) = viewModelScope.launch {
        repository.upsert(car)
    }

    fun deleteCar(car: Result) = viewModelScope.launch {
        repository.delete(car)
    }

    fun getAllSavedCars() = repository.getAllCars()

    private fun handleCarMakeResponse(response: Response<CarMakeResponse>): Resource<CarMakeResponse> {
        if (response.isSuccessful){
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleCarModelResponse(response: Response<CarMakeResponse>): Resource<CarMakeResponse> {
        if (response.isSuccessful){
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun getSafeCarMake(){
        carMake.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = repository.getAllMakes()
                carMake.postValue(handleCarMakeResponse(response))
            } else {
                carMake.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t : Throwable){
            when(t){
                is IOException -> carMake.postValue(Resource.Error("Network Failure"))
                else -> carMake.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun getSafeCarModel(makeId: Int){
        carModel.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = repository.getAllModel(makeId)
                carModel.postValue(handleCarModelResponse(response))
            } else {
                carModel.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t : Throwable){
            when(t){
                is IOException -> carModel.postValue(Resource.Error("Network Failure"))
                else -> carModel.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun hasInternetConnection() : Boolean{
        val connectivityManager = getApplication<CarApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when{
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when(type){
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

}