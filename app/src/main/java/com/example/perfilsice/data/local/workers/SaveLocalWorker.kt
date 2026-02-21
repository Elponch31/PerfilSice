package com.example.perfilsice.data.local.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.perfilsice.data.local.database.AppDatabase
import com.example.perfilsice.data.local.entity.AlumnoEntity

class SaveLocalWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // Datos de entrada provenientes del primer Worker (FetchSicenetWorker)
        val xml = inputData.getString("XML_RESULT") ?: return Result.failure()
        val funcion = inputData.getString("FUNCION") ?: return Result.failure()

        // Es mejor recibir la matrícula desde el ViewModel a través del primer Worker
        val matricula = inputData.getString("MATRICULA") ?: "123456"

        val dao = AppDatabase.getDatabase(applicationContext).alumnoDao()

        // Buscar si ya existe el alumno para no sobrescribir otros datos
        val entityExistente = dao.getAlumnoData(matricula) ?: AlumnoEntity(
            matricula = matricula,
            perfilRaw = "",
            cargaAcademicaRaw = "",
            kardexRaw = "",
            califUnidadRaw = "",
            califFinalRaw = ""
        )

        val nuevaEntity = when(funcion) {
            "PERFIL" -> entityExistente.copy(perfilRaw = xml, lastSync = System.currentTimeMillis())
            "CARGA" -> entityExistente.copy(cargaAcademicaRaw = xml, lastSync = System.currentTimeMillis())
            "KARDEX" -> entityExistente.copy(kardexRaw = xml, lastSync = System.currentTimeMillis())
            "CALIF_UNI" -> entityExistente.copy(califUnidadRaw = xml, lastSync = System.currentTimeMillis())
            "CALIF_FINAL" -> entityExistente.copy(califFinalRaw = xml, lastSync = System.currentTimeMillis())
            else -> entityExistente
        }

        dao.saveAlumnoData(nuevaEntity)
        return Result.success()
    }
}