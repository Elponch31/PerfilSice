package com.example.perfilsice.data.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType

class SicenetSoapClient {

    private val client = OkHttpClient()
    private var sessionCookie: String? = null

    fun login(matricula: String, password: String): Boolean {

        val soapBody = """
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                           xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                           xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <accesoLogin xmlns="http://tempuri.org/">
                  <strMatricula>$matricula</strMatricula>
                  <strContrasenia>$password</strContrasenia>
                  <tipoUsuario>ALUMNO</tipoUsuario>
                </accesoLogin>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        val requestBody = RequestBody.create(
            "text/xml; charset=utf-8".toMediaType(),
            soapBody
        )

        val request = Request.Builder()
            .url("https://sicenet.itsur.edu.mx/ws/wsalumnos.asmx")
            .post(requestBody)
            // 🔴 HEADERS IMPORTANTES (como el navegador)
            .addHeader("Content-Type", "text/xml; charset=utf-8")
            .addHeader("SOAPAction", "http://tempuri.org/accesoLogin")
            .addHeader("User-Agent", "Mozilla/5.0")
            .addHeader("Accept", "text/xml")
            .addHeader("Connection", "Keep-Alive")
            .build()

        val response = client.newCall(request).execute()

        // 🔹 Guardar cookie de sesión
        val cookies = response.headers("Set-Cookie")
        if (cookies.isNotEmpty()) {
            sessionCookie = cookies[0]
        }

        val responseXml = response.body?.string() ?: ""

        // SICENET responde aunque falle, por eso validamos contenido
        return responseXml.contains("<accesoLoginResult>")
    }

    fun getSessionCookie(): String? = sessionCookie
}
