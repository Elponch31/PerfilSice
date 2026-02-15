package com.example.perfilsice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calificacion_final")
data class CalificacionFinalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val matricula: String,
    val materia: String,
    val calificacionFinal: String
)
