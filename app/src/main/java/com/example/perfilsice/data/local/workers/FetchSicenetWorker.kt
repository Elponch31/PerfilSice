package com.example.perfilsice.data.local.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.perfilsice.data.repository.SicenetRepositoryImpl

class FetchSicenetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = SicenetRepositoryImpl(applicationContext)
        val funcion = inputData.getString("FUNCION") ?: "PERFIL"
        val matricula = inputData.getString("MATRICULA") ?: "123456"

        val resultado = when(funcion) {
            "PERFIL" -> repository.getPerfilAcademico()
            "CARGA" -> repository.getCargaAcademica()
            "KARDEX" -> repository.getKardex()
            "CALIF_UNI" -> repository.getCalificacionesUnidad()
            "CALIF_FINAL" -> repository.getCalificacionFinal()
            else -> ""
        }
        val startIndex = resultado.indexOfFirst { it == '{' || it == '[' }
        val endIndex = resultado.indexOfLast { it == '}' || it == ']' }

        if (startIndex == -1 || endIndex == -1) {
            return Result.failure()
        }

        var cleanJson = resultado.substring(startIndex, endIndex + 1)

        try {
            if (funcion == "KARDEX") {
                val array = if (cleanJson.startsWith("[")) {
                    org.json.JSONArray(cleanJson)
                } else {
                    val obj = org.json.JSONObject(cleanJson)
                    // Buscamos cualquier llave que se llame Kardex (sin importar si tiene falta de ortografía)
                    val key = obj.keys().asSequence().firstOrNull { it.contains("Kardex", ignoreCase = true) } ?: "IstKardex"
                    obj.optJSONArray(key) ?: org.json.JSONArray()
                }

                val newArray = org.json.JSONArray()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val newItem = org.json.JSONObject()
                    newItem.put("Materia", item.optString("Materia", ""))
                    newItem.put("Calif", item.optString("Calif", "0"))
                    newItem.put("S1", item.optString("S1", "0"))
                    newItem.put("Cdts", item.optString("Cdts", "0"))
                    newItem.put("Acred", item.optString("Acred", ""))
                    newArray.put(newItem)
                }
                cleanJson = newArray.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val outputData = workDataOf(
            "XML_RESULT" to cleanJson,
            "FUNCION" to funcion,
            "MATRICULA" to matricula
        )
        return Result.success(outputData)
    }
}