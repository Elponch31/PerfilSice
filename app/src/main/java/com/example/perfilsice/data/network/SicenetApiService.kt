package com.example.perfilsice.data.network

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface SicenetApiService {
    @Headers(
        "Content-Type: text/xml; charset=utf-8",
        "User-Agent: ksoap2-android/2.6.0+"
    )
    @POST
    suspend fun makeSoapRequest(
        @Url url: String,
        @Header("SOAPAction") soapAction: String,
        @Body body: RequestBody
    ): Response<String>
}