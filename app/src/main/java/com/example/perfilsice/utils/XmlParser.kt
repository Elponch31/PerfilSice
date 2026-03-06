package com.example.perfilsice.utils

object XmlParser {

    fun extraerContenidoXml(xml: String, tag: String): String {
        val startTag = "<$tag>"
        val endTag = "</$tag>"

        if (!xml.contains(startTag) || !xml.contains(endTag)) {
            return "Cargando datos o no se encontró la información..."
        }

        return xml.substringAfter(startTag)
            .substringBefore(endTag)
            .replace("<![CDATA[", "")
            .replace("]]>", "")
            .trim()
    }
}

// MODELO PARA CARGA ACADEMICA

data class MateriaCarga(
    val materia: String,
    val docente: String,
    val grupo: String,
    val creditos: String
)

fun parsearCargaAcademica(jsonString: String): List<MateriaCarga> {
    val lista = mutableListOf<MateriaCarga>()
    try {
        val jsonArray = org.json.JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            lista.add(
                MateriaCarga(
                    materia = item.optString("materia", item.optString("Materia", "Materia Desconocida")),
                    docente = item.optString("docente", item.optString("Docente", "Sin docente asignado")),
                    grupo = item.optString("grupo", item.optString("Grupo", "N/A")),
                    creditos = item.optString("creditos", item.optString("Creditos", "0"))
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return lista
}

// MODELO PARA PERFIL ACADEMICO
data class PerfilAcademico(
    val nombre: String,
    val matricula: String,
    val carrera: String,
    val especialidad: String,
    val semActual: String,
    val estatus: String,
    val cdtosAcumulados: String
)

fun parsearPerfilAcademico(jsonString: String): PerfilAcademico? {
    return try {
        val jsonObject = org.json.JSONObject(jsonString)
        PerfilAcademico(
            nombre = jsonObject.optString("nombre", "Sin nombre"),
            matricula = jsonObject.optString("matricula", "N/A"),
            carrera = jsonObject.optString("carrera", "N/A"),
            especialidad = jsonObject.optString("especialidad", "N/A"),
            semActual = jsonObject.optString("semActual", "N/A"),
            estatus = jsonObject.optString("estatus", "N/A"),
            cdtosAcumulados = jsonObject.optString("cdtosAcumulados", "0")
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// MODELO PARA KARDEX
data class MateriaKardex(
    val materia: String,
    val calificacion: String,
    val semestre: String,
    val creditos: String,
    val tipoEvaluacion: String
)

fun parsearKardex(jsonString: String): List<MateriaKardex> {
    val lista = mutableListOf<MateriaKardex>()
    try {
        // 1. EXTRACCIÓN AGRESIVA:
        // Buscamos como un láser exactamente dónde abre la primera llave '{' o '['
        // y dónde cierra la última '}' o ']', ignorando cualquier basura invisible antes o después.
        val startIndex = jsonString.indexOfFirst { it == '{' || it == '[' }
        val endIndex = jsonString.indexOfLast { it == '}' || it == ']' }

        // Si por algún motivo no hay formato JSON, abortamos limpiamente
        if (startIndex == -1 || endIndex == -1) return lista

        val pureJson = jsonString.substring(startIndex, endIndex + 1)

        // 2. Leer el JSON purificado
        val jsonArray = if (pureJson.startsWith("[")) {
            org.json.JSONArray(pureJson)
        } else {
            val jsonObject = org.json.JSONObject(pureJson)

            // 3. BÚSQUEDA A CIEGAS: En lugar de adivinar si Sicenet le puso "IstKardex" o "lstKardex",
            // le decimos al código que extraiga el PRIMER arreglo de datos que encuentre adentro.
            var arrayEncontrado: org.json.JSONArray? = null
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val posibleArreglo = jsonObject.optJSONArray(key)
                if (posibleArreglo != null) {
                    arrayEncontrado = posibleArreglo
                    break // Encontramos la lista de materias, dejamos de buscar
                }
            }
            arrayEncontrado ?: org.json.JSONArray()
        }

        // 4. Llenamos las tarjetas, protegiendo cada materia con su propio bloque try-catch
        for (i in 0 until jsonArray.length()) {
            try {
                val item = jsonArray.getJSONObject(i)
                lista.add(
                    MateriaKardex(
                        materia = item.optString("Materia", "Desconocida"),
                        calificacion = item.optString("Calif", "0"),
                        semestre = item.optString("S1", "0"),
                        creditos = item.optString("Cdts", "0"),
                        tipoEvaluacion = item.optString("Acred", "")
                    )
                )
            } catch (e: Exception) {
                // Si una sola materia viene corrupta, la ignoramos pero SALVAMOS el resto del semestre
            }
        }
    } catch (e: Exception) {
        // Si hay un error profundo, lo imprimimos en consola pero no cerramos la app
        e.printStackTrace()
    }
    return lista
}

// MODELO PARA CALIFICACION POR UNIDAD
data class CalificacionUnidad(
    val materia: String,
    val unidades: List<String>
)

fun parsearCalifUnidades(jsonString: String): List<CalificacionUnidad> {
    val lista = mutableListOf<CalificacionUnidad>()
    try {
        val jsonArray = org.json.JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val materia = item.optString("Materia", item.optString("materia", "Desconocida"))
            val calificaciones = mutableListOf<String>()

            for (j in 1..13) {
                val calif = item.optString("C$j", "")
                if (calif.isNotEmpty() && calif != "null") {
                    calificaciones.add(calif)
                }
            }
            lista.add(CalificacionUnidad(materia, calificaciones))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return lista
}



data class CalificacionFinal(
    val materia: String,
    val calificacion: String,
    val observaciones: String
)

fun parsearCalificacionFinal(jsonString: String): List<CalificacionFinal> {
    val lista = mutableListOf<CalificacionFinal>()
    try {
        val jsonArray = org.json.JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            lista.add(
                CalificacionFinal(
                    materia = item.optString("Materia", item.optString("materia", "Desconocida")),
                    calificacion = item.optString("CalifFinal", item.optString("califFinal", item.optString("Calificacion", item.optString("Calif", "0")))),
                    observaciones = item.optString("Observaciones", item.optString("observaciones", item.optString("Acred", "")))
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return lista
}