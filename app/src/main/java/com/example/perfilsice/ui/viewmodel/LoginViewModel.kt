package com.example.perfilsice.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.perfilsice.data.local.entity.AlumnoEntity
import com.example.perfilsice.data.local.workers.FetchSicenetWorker
import com.example.perfilsice.data.local.workers.SaveLocalWorker
import com.example.perfilsice.data.repository.SicenetRepository
import com.example.perfilsice.data.repository.SicenetRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SicenetRepository = SicenetRepositoryImpl(application)

    var loginSuccess by mutableStateOf(false)
    var profileData by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var currentSection by mutableStateOf("PERFIL")
    var currentMatricula by mutableStateOf("")
    var offlineMessage by mutableStateOf("")

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun loginAndSyncData(matricula: String, password: String) {
        viewModelScope.launch {
            currentMatricula = matricula
            isLoading = true
            errorMessage = ""

            try {
                val success = withContext(Dispatchers.IO) {
                    repository.login(matricula, password)
                }

                if (success) {
                    loginSuccess = true
                    syncData("PERFIL")
                } else {
                    errorMessage = "Credenciales incorrectas o error de conexión"
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = "Error en el login: ${e.message}"
                isLoading = false
            }
        }
    }

    fun syncData(funcion: String) {
        isLoading = true
        errorMessage = ""
        val workManager = WorkManager.getInstance(getApplication())

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val fetchRequest = OneTimeWorkRequestBuilder<FetchSicenetWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf("FUNCION" to funcion,
                "MATRICULA" to currentMatricula ))
            .build()

        val saveRequest = OneTimeWorkRequestBuilder<SaveLocalWorker>()
            .build()

        workManager.beginUniqueWork(
            "sync_$funcion",
            ExistingWorkPolicy.REPLACE,
            fetchRequest
        ).then(saveRequest).enqueue()

        workManager.getWorkInfoByIdLiveData(fetchRequest.id).observeForever { workInfo ->
            if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                val xml = workInfo.outputData.getString("XML_RESULT")
                if (xml != null) {
                    this.profileData = xml
                }
                this.isLoading = false
            } else if (workInfo?.state == WorkInfo.State.FAILED) {
                this.errorMessage = "Error al sincronizar con el servidor"
                this.isLoading = false
            }
        }
    }

    fun cargarInformacion(matricula: String, funcion: String) {
        viewModelScope.launch {
            profileData = ""
            if (isNetworkAvailable()) {
                offlineMessage = ""
                syncData(funcion)
            } else {
                val localData = withContext(Dispatchers.IO) {
                    repository.getAlumnoDataLocal(matricula)
                }

                if (localData != null) {
                    offlineMessage = "Modo Offline"

                    profileData = when(funcion) {
                        "CARGA" -> localData.cargaAcademicaRaw
                        "KARDEX" -> localData.kardexRaw
                        "CALIF_UNI" -> localData.califUnidadRaw
                        "CALIF_FINAL" -> localData.califFinalRaw
                        else -> localData.perfilRaw
                    }
                    loginSuccess = true
                } else {
                    errorMessage = "No hay datos locales guardados y no tienes conexión a internet."
                }
            }
        }
    }
    
}