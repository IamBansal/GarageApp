package com.example.garageapp.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.garageapp.repository.CarRepository

@Suppress("UNCHECKED_CAST")
class CarViewModelProviderFactory(
    val app: Application,
    private val repository: CarRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CarViewModel(app, repository) as T
    }

}