package com.example.perfilsice.data.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://sicenet.surguanajuato.tecnm.mx/"

    fun create(soapClient: SicenetSoapClient): SicenetSoapService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(soapClient.getClient())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(SicenetSoapService::class.java)
    }
}
