package com.example.perfilsice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.perfilsice.data.local.entity.AlumnoEntity

@Dao
interface AlumnoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAlumnoData(alumno: AlumnoEntity)

    @Query("SELECT * FROM datos_alumno WHERE matricula = :matricula")
    suspend fun getAlumnoData(matricula: String): AlumnoEntity?
}