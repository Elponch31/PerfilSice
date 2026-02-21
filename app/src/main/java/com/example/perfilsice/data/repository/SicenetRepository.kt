package com.example.perfilsice.data.repository

interface SicenetRepository {
    suspend fun login(matricula: String, password: String): Boolean
    suspend fun getPerfilAcademico(): String
    suspend fun getCargaAcademica(): String
    suspend fun getKardex(): String
    suspend fun getCalificacionesUnidad(): String
    suspend fun getCalificacionFinal(): String
}
