package com.example.perfilsice.data.local.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.perfilsice.data.repository.SicenetRepositoryImpl

class FetchSicenetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SicenetRepositoryImpl()
        val funcion = inputData.getString("FUNCION") ?: "PERFIL"

        // Simular o realizar login previo si es necesario para obtener la URL de sesión
        // Aquí asumimos que el repositorio ya gestiona la sesión activa

        val resultado = when(funcion) {
            "PERFIL" -> repository.getPerfilAcademico()
            "CARGA" -> repository.getCargaAcademica()
            "KARDEX" -> repository.getKardex()
            "CALIF_UNI" -> repository.getCalificacionesUnidad()
            "CALIF_FINAL" -> repository.getCalificacionFinal()
            else -> ""
        }

        return if (resultado.isNotEmpty() && !resultado.contains("Error")) {
            val outputData = workDataOf(
                "XML_RESULT" to resultado,
                "FUNCION" to funcion,
                "MATRICULA" to (inputData.getString("MATRICULA") ?: "123456")
            )
            return Result.success(outputData)
        } else {
            Result.failure()
        }

    }
}