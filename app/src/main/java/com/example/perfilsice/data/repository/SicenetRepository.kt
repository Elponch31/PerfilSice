package com.example.perfilsice.data.repository

interface SicenetRepository {
    suspend fun login(matricula: String, password: String): Boolean
    suspend fun getPerfilAcademico(): String
}
