package com.example.perfilsice.data.network

import android.annotation.SuppressLint
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class SicenetSoapClient {

    private val client = getUnsafeOkHttpClient()
    private var sessionUrl: String? = null
    private var sessionCookie: String? = null

    private val BASE_DOMAIN = "https://sicenet.surguanajuato.tecnm.mx"
    private val INITIAL_URL = "$BASE_DOMAIN/ws/wsalumnos.asmx"

    fun login(matricula: String, password: String): Boolean {
        val soapBody = """
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                           xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                           xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <accesoLogin xmlns="http://tempuri.org/">
                  <strMatricula><![CDATA[$matricula]]></strMatricula>
                  <strContrasenia><![CDATA[$password]]></strContrasenia>
                  <tipoUsuario>ALUMNO</tipoUsuario>
                </accesoLogin>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        val mediaType = "text/xml; charset=utf-8".toMediaType()
        val requestBody = RequestBody.create(mediaType, soapBody)

        var currentUrl = INITIAL_URL
        var attemptCount = 0
        val maxRedirects = 5

        while (attemptCount < maxRedirects) {
            attemptCount++

            val request = Request.Builder()
                .url(currentUrl)
                .post(requestBody)
                .header("SOAPAction", "\"http://tempuri.org/accesoLogin\"")
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("User-Agent", "ksoap2-android/2.6.0+")
                .build()

            try {
                val response = client.newCall(request).execute()
                val code = response.code

                if (code in listOf(301, 302, 307)) {
                    val location = response.header("Location")
                    response.close()
                    if (location != null) {
                        currentUrl = if (location.startsWith("http")) location else "$BASE_DOMAIN$location"
                        continue
                    }
                }

                val responseXml = response.body?.string() ?: ""

                val cookies = response.headers("Set-Cookie")
                if (cookies.isNotEmpty()) {
                    sessionCookie = cookies.joinToString("; ") { it.split(";")[0] }
                }

                if (code == 200 && responseXml.contains("<accesoLoginResult>")) {
                    sessionUrl = currentUrl
                    return !responseXml.contains("false")
                }

                return false

            } catch (e: Exception) {
                return false
            }
        }
        return false
    }

    fun getAlumnoAcademico(): String {
        val targetUrl = sessionUrl ?: return "Error: No hay sesión activa."

        val soapBody = """
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                           xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                           xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" />
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        val requestBody = RequestBody.create(
            "text/xml; charset=utf-8".toMediaType(),
            soapBody
        )

        val requestBuilder = Request.Builder()
            .url(targetUrl)
            .post(requestBody)
            .header("SOAPAction", "\"http://tempuri.org/getAlumnoAcademicoWithLineamiento\"")
            .header("Content-Type", "text/xml; charset=utf-8")

        sessionCookie?.let { requestBuilder.addHeader("Cookie", it) }

        return try {
            val response = client.newCall(requestBuilder.build()).execute()
            response.body?.string() ?: "Error"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(
            @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }


    fun getClient(): OkHttpClient = client
}
