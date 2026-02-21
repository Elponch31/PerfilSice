package com.example.perfilsice.data.local.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.perfilsice.data.local.entity.AlumnoEntity
import com.example.perfilsice.data.repository.SicenetRepositoryImpl

class SaveLocalWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val xml = inputData.getString("XML_RESULT") ?: return Result.failure()
        val funcion = inputData.getString("FUNCION") ?: return Result.failure()
        val matricula = inputData.getString("MATRICULA") ?: "123456"

        val repository = SicenetRepositoryImpl(applicationContext)

        val entityExistente = repository.getAlumnoDataLocal(matricula) ?: AlumnoEntity(
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

        repository.saveAlumnoDataLocal(nuevaEntity)

        return Result.success()
    }
}