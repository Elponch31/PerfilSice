package com.example.perfilsice.data.network

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SicenetSoapService {
    @POST("ws/wsalumnos.asmx")
    suspend fun accesoLogin(
        @Header("SOAPAction") soapAction: String,
        @Header("Content-Type") contentType: String,
        @Body body: RequestBody
    ): Response<String>

    @POST("ws/wsalumnos.asmx")
    suspend fun getAlumnoAcademico(
        @Header("SOAPAction") soapAction: String,
        @Header("Content-Type") contentType: String,
        @Body body: RequestBody
    ): Response<String>
}
