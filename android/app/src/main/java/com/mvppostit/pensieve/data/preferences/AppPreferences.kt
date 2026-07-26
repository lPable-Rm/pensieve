package com.mvppostit.pensieve.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mvppostit.pensieve.ui.theme.PaletteId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Preferencias pequeñas y locales que no forman parte del contenido de Room. */
class AppPreferences(context: Context) {

    private val dataStore = context.applicationContext.appPreferencesDataStore

    /** Un valor ausente equivale a no haber completado todavía el onboarding. */
    val onboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { exception ->
            // Una lectura incompleta del archivo no debe dejar la actividad sin
            // contenido. Solo tratamos el error de E/S como preferencias vacías;
            // otros fallos siguen propagándose para no ocultar errores reales.
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] ?: false
        }

    /** La ausencia o corrupción del identificador vuelve a la paleta lavanda. */
    val colorPalette: Flow<PaletteId> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            PaletteId.fromStorage(preferences[Keys.COLOR_PALETTE])
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    /** Guardamos el nombre estable, nunca los valores RGB de la paleta. */
    suspend fun setColorPalette(paletteId: PaletteId) {
        dataStore.edit { preferences ->
            preferences[Keys.COLOR_PALETTE] = paletteId.storageId
        }
    }

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val COLOR_PALETTE = stringPreferencesKey("color_palette")
    }
}

private val Context.appPreferencesDataStore by preferencesDataStore(
    name = "app_preferences",
)
