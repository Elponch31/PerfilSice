package com.example.perfilsice.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfilsice.data.repository.SicenetRepository
import com.example.perfilsice.data.repository.SicenetRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {
    private val repository: SicenetRepository = SicenetRepositoryImpl()

    var loginSuccess by mutableStateOf(false)
    var profileData by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    fun loginAndFetchProfile(matricula: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""

            // Como usamos Retrofit + Suspend functions, ya son asíncronas,
            // pero mantenemos Dispatchers.IO para estar seguros de no bloquear el hilo principal.
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
                // Aquí podrías mover esta lógica de limpieza al repositorio también si quisieras ser más estricto
                profileData = limpiarRespuestaXml(perfilResponse)
                loginSuccess = true
            } else {
                // Si ya había un error técnico seteado, no lo sobreescribas con credenciales incorrectas
                if (errorMessage.isEmpty()) {
                    errorMessage = "Credenciales incorrectas o error de conexión"
                }
            }
            isLoading = false
        }
    }

    private fun limpiarRespuestaXml(xml: String): String {
        return try {
            xml.substringAfter("<getAlumnoAcademicoWithLineamientoResult>")
                .substringBefore("</getAlumnoAcademicoWithLineamientoResult>")
        } catch (e: Exception) {
            xml
        }
    }
}