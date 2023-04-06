package com.example.garageapp.utils

class Constants {

    companion object {

        const val BASE_URL = "https://vpic.nhtsa.dot.gov/"
        const val MAKE_ENDPOINT = "api/vehicles/getallmakes?format=json"
        const val MODEL_ENDPOINT = "api/vehicles/GetModelsForMakeId/{makeId}?format=json"

    }

}