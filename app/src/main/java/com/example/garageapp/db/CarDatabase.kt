package com.example.garageapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.garageapp.model.Result

@Database(
    entities = [Result::class],
    version = 3
)
abstract class CarDatabase : RoomDatabase(){

    abstract fun getArticleDao(): CarDao

    companion object {

        @Volatile
        private var instance: CarDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            CarDatabase::class.java,
            "cars_db.db"
        ).fallbackToDestructiveMigration()
            .build()


    }


}