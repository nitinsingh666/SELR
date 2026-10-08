package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EmergencyContactEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SelrDatabase : RoomDatabase() {

    abstract fun emergencyContactDao(): EmergencyContactDao

    companion object {
        @Volatile
        private var INSTANCE: SelrDatabase? = null

        fun getDatabase(context: Context): SelrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SelrDatabase::class.java,
                    "selr_emergency_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
