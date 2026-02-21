package com.example.perfilsice.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.perfilsice.data.local.dao.AlumnoDao
import com.example.perfilsice.data.local.entity.AlumnoEntity

@Database(entities = [AlumnoEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alumnoDao(): AlumnoDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "perfil_sice_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}