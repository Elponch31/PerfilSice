package com.example.perfilsice.data.repository

import com.example.perfilsice.data.network.SicenetSoapClient

class SicenetRepository {

    private val soapClient = SicenetSoapClient()

    fun login(matricula: String, password: String): Boolean {
        return soapClient.login(matricula, password)
    }

    fun getPerfilAcademico(): String {
        return soapClient.getAlumnoAcademico()
    }
}
