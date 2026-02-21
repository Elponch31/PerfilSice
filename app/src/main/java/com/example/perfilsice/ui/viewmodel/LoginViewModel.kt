package com.example.perfilsice.ui.viewmodel

import android.app.Application
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
import com.example.perfilsice.data.local.database.AppDatabase
import com.example.perfilsice.data.local.entity.AlumnoEntity
import com.example.perfilsice.data.local.workers.FetchSicenetWorker
import com.example.perfilsice.data.local.workers.SaveLocalWorker
import com.example.perfilsice.data.repository.SicenetRepository
import com.example.perfilsice.data.repository.SicenetRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.perfilsice.utils.XmlParser

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SicenetRepository = SicenetRepositoryImpl()
    private val alumnoDao = AppDatabase.getDatabase(application).alumnoDao()

    var loginSuccess by mutableStateOf(false)
    var profileData by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var currentSection by mutableStateOf("PERFIL")
    var currentMatricula by mutableStateOf("")

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
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
                    // 1. Descargar todos los datos desde el servicio web
                    val perfilXml = withContext(Dispatchers.IO) { repository.getPerfilAcademico() }
                    val cargaXml = withContext(Dispatchers.IO) { repository.getCargaAcademica() }
                    val kardexXml = withContext(Dispatchers.IO) { repository.getKardex() }
                    val califUnidadXml = withContext(Dispatchers.IO) { repository.getCalificacionesUnidad() }
                    val califFinalXml = withContext(Dispatchers.IO) { repository.getCalificacionFinal() }

                    // 2. Crear el objeto de entidad para Room
                    val alumno = AlumnoEntity(
                        matricula = matricula,
                        perfilRaw = perfilXml,
                        cargaAcademicaRaw = cargaXml,
                        kardexRaw = kardexXml,
                        califUnidadRaw = califUnidadXml,
                        califFinalRaw = califFinalXml
                    )

                    // 3. Guardar en la base de datos local
                    withContext(Dispatchers.IO) {
                        alumnoDao.saveAlumnoData(alumno)
                    }

                    // 4. Actualizar UI (puedes limpiar el XML para mostrar solo lo relevante)
                    profileData = perfilXml
                    loginSuccess = true

                } else {
                    errorMessage = "Credenciales incorrectas o error de conexión"
                }
            } catch (e: Exception) {
                errorMessage = "Error en la sincronización: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun syncData(funcion: String) {
        val workManager = WorkManager.getInstance(getApplication())

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val fetchRequest = OneTimeWorkRequestBuilder<FetchSicenetWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf("FUNCION" to funcion))
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
                // Obtenemos el XML que el FetchSicenetWorker puso en outputData
                val xml = workInfo.outputData.getString("XML_RESULT")
                if (xml != null) {
                    this.profileData = xml // Esto refrescará la UI automáticamente
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
            if (isNetworkAvailable()) {
                // Caso con internet: disparar WorkManager para sincronizar
                syncData(funcion)
            } else {
                // Caso sin internet: consulta local en Room
                val localData = withContext(Dispatchers.IO) {
                    alumnoDao.getAlumnoData(matricula)
                }

                if (localData != null) {
                    val fecha = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(localData.lastSync))

                    profileData = "Modo Offline - Última actualización: $fecha\n\n" +
                            when(funcion) {
                                "CARGA" -> localData.cargaAcademicaRaw
                                "KARDEX" -> localData.kardexRaw
                                "CALIF_UNI" -> localData.califUnidadRaw
                                "CALIF_FINAL" -> localData.califFinalRaw
                                else -> localData.perfilRaw
                            }
                    loginSuccess = true // Para permitir la navegación a la ProfileScreen
                } else {
                    errorMessage = "No hay datos locales guardados y no tienes conexión a internet."
                }
            }
        }
    }

    fun cambiarSeccion(matricula: String, nuevaSeccion: String) {
        currentSection = nuevaSeccion

        viewModelScope.launch {
            val localData = withContext(Dispatchers.IO) {
                alumnoDao.getAlumnoData(matricula)
            }

            if (localData != null) {
                profileData = when(nuevaSeccion) {
                    "PERFIL" -> localData.perfilRaw
                    "CARGA" -> localData.cargaAcademicaRaw
                    "KARDEX" -> localData.kardexRaw
                    "CALIF_UNI" -> localData.califUnidadRaw
                    "CALIF_FINAL" -> localData.califFinalRaw
                    else -> ""
                }
            }
        }
    }
}