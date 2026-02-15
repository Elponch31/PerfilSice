package com.example.perfilsice.data.repository

import android.content.Context
import com.example.perfilsice.data.local.database.SicenetDatabase
import com.example.perfilsice.data.local.entity.*

class LocalSicenetRepository(context: Context) {

    private val dao = SicenetDatabase
        .getDatabase(context)
        .sicenetDao()

    // GUARDAR
    suspend fun saveCargaAcademica(data: List<CargaAcademicaEntity>) {
        dao.insertCargaAcademica(data)
    }

    suspend fun saveKardex(data: List<KardexEntity>) {
        dao.insertKardex(data)
    }

    suspend fun saveCalificacionesUnidad(data: List<CalificacionUnidadEntity>) {
        dao.insertCalificacionesUnidad(data)
    }

    suspend fun saveCalificacionFinal(data: List<CalificacionFinalEntity>) {
        dao.insertCalificacionFinal(data)
    }

    // CONSULTAR
    suspend fun getCargaAcademica(matricula: String) =
        dao.getCargaAcademica(matricula)

    suspend fun getKardex(matricula: String) =
        dao.getKardex(matricula)

    suspend fun getCalificacionesUnidad(matricula: String) =
        dao.getCalificacionesUnidad(matricula)

    suspend fun getCalificacionFinal(matricula: String) =
        dao.getCalificacionFinal(matricula)
}
