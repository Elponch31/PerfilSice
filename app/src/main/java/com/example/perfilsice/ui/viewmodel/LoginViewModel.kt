package com.example.perfilsice.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfilsice.data.repository.SicenetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {
    private val repository = SicenetRepository()

    var loginSuccess by mutableStateOf(false)
    var profileData by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    fun loginAndFetchProfile(matricula: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""

            val success = withContext(Dispatchers.IO) {
                try {
                    repository.login(matricula, password)
                } catch (e: Exception) {
                    errorMessage = "Error técnico: ${e.message}"
                    false
                }
            }
            if (success) {
                val perfilResponse = withContext(Dispatchers.IO) {
                    repository.getPerfilAcademico()
                }
                profileData = limpiarRespuestaXml(perfilResponse)
                loginSuccess = true
            } else {
               errorMessage = "Credenciales incorrectas o error de conexión"
            }
            isLoading = false
        }
    }

    private fun limpiarRespuestaXml(xml: String): String {
        return xml.substringAfter("<getAlumnoAcademicoWithLineamientoResult>")
            .substringBefore("</getAlumnoAcademicoWithLineamientoResult>")
    }
}