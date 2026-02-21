package com.example.perfilsice.data.repository

import android.util.Log
import com.example.perfilsice.data.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class SicenetRepositoryImpl : SicenetRepository {
    private val api = RetrofitClient.apiService
    private var currentSessionUrl: String? = null
    private var sessionCookie: String? = null

    private val BASE_DOMAIN = "https://sicenet.surguanajuato.tecnm.mx"
    private val INITIAL_URL = "$BASE_DOMAIN/ws/wsalumnos.asmx"

    override suspend fun login(matricula: String, password: String): Boolean {
        val soapXml = """
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <accesoLogin xmlns="http://tempuri.org/">
                  <strMatricula><![CDATA[$matricula]]></strMatricula>
                  <strContrasenia><![CDATA[$password]]></strContrasenia>
                  <tipoUsuario>ALUMNO</tipoUsuario>
                </accesoLogin>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        val requestBody = soapXml.toRequestBody("text/xml; charset=utf-8".toMediaType())
        var currentUrl = INITIAL_URL
        var attemptCount = 0

        // Lógica de "Perseguir el Redirect"
        while (attemptCount < 5) {
            attemptCount++
            try {
                Log.d("SICE_RETROFIT", "Intento #$attemptCount POST a: $currentUrl")

                val response = api.makeSoapRequest(
                    url = currentUrl,
                    soapAction = "\"http://tempuri.org/accesoLogin\"",
                    body = requestBody
                )

                // Capturar Cookies si existen
                val cookies = response.headers().values("Set-Cookie")
                if (cookies.isNotEmpty()) {
                    sessionCookie = cookies.joinToString("; ") { it.split(";")[0] }
                }

                // Manejo manual de redirecciones (301, 302, 307)
                if (response.code() in 300..399) {
                    val location = response.headers()["Location"]
                    if (location != null) {
                        currentUrl = if (location.startsWith("http")) location else "$BASE_DOMAIN$location"
                        continue // Reintentamos con la nueva URL
                    }
                }

                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    if (responseBody.contains("<accesoLoginResult>")) {
                        // ¡Éxito! Guardamos la URL final que tiene la sesión
                        currentSessionUrl = currentUrl
                        return !responseBody.contains("false") // Retorna true si no dice "false"
                    }
                }

            } catch (e: Exception) {
                Log.e("SICE_RETROFIT", "Error: ${e.message}")
                return false
            }
        }
        return false
    }

    override suspend fun getPerfilAcademico(): String {
        val targetUrl = currentSessionUrl ?: return "Error: No hay sesión activa."

        val soapXml = """
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" />
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        val requestBody = soapXml.toRequestBody("text/xml; charset=utf-8".toMediaType())

        return try {
            val response = api.makeSoapRequest(
                url = targetUrl,
                soapAction = "\"http://tempuri.org/getAlumnoAcademicoWithLineamiento\"",
                body = requestBody
            )

            if (response.isSuccessful) {
                response.body() ?: "Error: Respuesta vacía"
            } else {
                "Error HTTP: ${response.code()}"
            }
        } catch (e: Exception) {
            "Error Excepción: ${e.message}"
        }
    }

    override suspend fun getCargaAcademica(): String =
        performSoapRequest("getCargaAcademicaByAlumno", """<getCargaAcademicaByAlumno xmlns="http://tempuri.org/" />""")

    override suspend fun getKardex(): String =
        performSoapRequest("getAllKardexConPromedioByAlumno", """
        <getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/">
            <aluLineamiento>1</aluLineamiento>
        </getAllKardexConPromedioByAlumno>
    """.trimIndent())

    override suspend fun getCalificacionesUnidad(): String =
        performSoapRequest("getCalifUnidadesByAlumno", """<getCalifUnidadesByAlumno xmlns="http://tempuri.org/" />""")

    override suspend fun getCalificacionFinal(): String =
        performSoapRequest("getAllCalifFinalByAlumnos", """
        <getAllCalifFinalByAlumnos xmlns="http://tempuri.org/">
            <bytModEducativo>1</bytModEducativo>
        </getAllCalifFinalByAlumnos>
    """.trimIndent())

    private suspend fun performSoapRequest(action: String, bodyContent: String): String {
        val targetUrl = currentSessionUrl ?: return "Error: Sin sesión"
        val soapXml = """
        <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            $bodyContent
          </soap:Body>
        </soap:Envelope>
    """.trimIndent()

        val requestBody = soapXml.toRequestBody("text/xml; charset=utf-8".toMediaType())
        val response = api.makeSoapRequest(targetUrl, "\"http://tempuri.org/$action\"", requestBody)

        return if (response.isSuccessful) response.body() ?: "" else "Error ${response.code()}"
    }
}