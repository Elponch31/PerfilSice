package com.example.perfilsice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import android.database.Cursor
import com.example.perfilsice.data.local.entity.AlumnoEntity

@Dao
interface AlumnoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAlumnoData(alumno: AlumnoEntity)

    @Query("SELECT * FROM datos_alumno WHERE matricula = :matricula")
    suspend fun getAlumnoData(matricula: String): AlumnoEntity?

    @Query("SELECT * FROM datos_alumno")
    fun getAllAlumnosCursor(): Cursor

    @Query("UPDATE datos_alumno SET cargaAcademicaRaw = :nuevaCarga WHERE matricula = :matricula")
    fun updateCargaAcademicaDirecto(matricula: String, nuevaCarga: String): Int

    @Query("UPDATE datos_alumno SET kardexRaw = :nuevoKardex WHERE matricula = :matricula")
    fun updateKardexDirecto(matricula: String, nuevoKardex: String): Int
}