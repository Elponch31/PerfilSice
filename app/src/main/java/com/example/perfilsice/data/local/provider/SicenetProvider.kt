package com.example.perfilsice.data.local.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.perfilsice.data.local.database.AppDatabase

class SicenetProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.perfilsice.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/alumnos")

        const val ALUMNOS = 1
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, "alumnos", ALUMNOS)
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val context = context ?: return null
        val dao = AppDatabase.getDatabase(context).alumnoDao()

        return when (uriMatcher.match(uri)) {
            ALUMNOS -> dao.getAllAlumnosCursor() // Devuelve Kárdex y Carga
            else -> throw IllegalArgumentException("URI desconocida: $uri")
        }
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val context = context ?: return 0
        val dao = AppDatabase.getDatabase(context).alumnoDao()

        return when (uriMatcher.match(uri)) {
            ALUMNOS -> {
                val matricula = selectionArgs?.get(0) ?: return 0
                var filasModificadas = 0
                if (values?.containsKey("cargaAcademicaRaw") == true) {
                    val nuevaCarga = values.getAsString("cargaAcademicaRaw")
                    filasModificadas += dao.updateCargaAcademicaDirecto(matricula, nuevaCarga)
                }
                if (values?.containsKey("kardexRaw") == true) {
                    val nuevoKardex = values.getAsString("kardexRaw")
                    filasModificadas += dao.updateKardexDirecto(matricula, nuevoKardex)
                }
                return filasModificadas
            }
            else -> throw IllegalArgumentException("URI desconocida: $uri")
        }
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/vnd.$AUTHORITY.alumnos"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
}