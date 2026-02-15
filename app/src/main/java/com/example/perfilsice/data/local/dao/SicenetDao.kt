package com.example.perfilsice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.perfilsice.data.local.entity.*

@Dao
interface SicenetDao {

    // INSERTS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCargaAcademica(data: List<CargaAcademicaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKardex(data: List<KardexEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalificacionesUnidad(data: List<CalificacionUnidadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalificacionFinal(data: List<CalificacionFinalEntity>)

    // CONSULTAS
    @Query("SELECT * FROM carga_academica WHERE matricula = :matricula")
    suspend fun getCargaAcademica(matricula: String): List<CargaAcademicaEntity>

    @Query("SELECT * FROM kardex WHERE matricula = :matricula")
    suspend fun getKardex(matricula: String): List<KardexEntity>

    @Query("SELECT * FROM calificaciones_unidad WHERE matricula = :matricula")
    suspend fun getCalificacionesUnidad(matricula: String): List<CalificacionUnidadEntity>

    @Query("SELECT * FROM calificacion_final WHERE matricula = :matricula")
    suspend fun getCalificacionFinal(matricula: String): List<CalificacionFinalEntity>
}
