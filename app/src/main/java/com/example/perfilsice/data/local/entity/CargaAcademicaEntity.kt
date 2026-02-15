package com.example.perfilsice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carga_academica")
data class CargaAcademicaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val matricula: String,
    val materia: String,
    val grupo: String,
    val docente: String
)
