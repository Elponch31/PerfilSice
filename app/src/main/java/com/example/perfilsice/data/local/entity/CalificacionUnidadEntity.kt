package com.example.perfilsice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calificaciones_unidad")
data class CalificacionUnidadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val matricula: String,
    val materia: String,
    val unidad: String,
    val calificacion: String
)
