package com.example.perfilsice.data.repository

import com.example.perfilsice.data.network.RetrofitClient
import com.example.perfilsice.data.network.SicenetSoapClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody

class SicenetRepository {

    private val soapClient = SicenetSoapClient()
    private val retrofitService = RetrofitClient.create(soapClient)


    fun login(matricula: String, password: String): Boolean {
        return soapClient.login(matricula, password)
    }

    fun getPerfilAcademico(): String {
        return soapClient.getAlumnoAcademico()
    }

    suspend fun loginRetrofit(matricula: String, password: String): String {
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

        val body = RequestBody.create(
            "text/xml; charset=utf-8".toMediaType(),
            soapBody
        )

        val response = retrofitService.accesoLogin(
            "\"http://tempuri.org/accesoLogin\"",
            "text/xml; charset=utf-8",
            body
        )

        return response.body() ?: "Error"
    }
}
