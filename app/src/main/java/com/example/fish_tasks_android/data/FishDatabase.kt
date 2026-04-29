package com.example.fish_tasks_android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fish_tasks_android.model.Task

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class FishDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var Instance: FishDatabase? = null

        fun getDatabase(context: Context): FishDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, FishDatabase::class.java, "fish_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
