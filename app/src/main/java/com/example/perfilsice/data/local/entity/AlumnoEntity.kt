package com.example.perfilsice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "datos_alumno")
data class AlumnoEntity(
    @PrimaryKey val matricula: String,
    val perfilRaw: String,
    val cargaAcademicaRaw: String,
    val kardexRaw: String,
    val califUnidadRaw: String,
    val califFinalRaw: String,
    val lastSync: Long = System.currentTimeMillis()
)