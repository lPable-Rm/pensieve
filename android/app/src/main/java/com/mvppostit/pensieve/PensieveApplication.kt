package com.mvppostit.pensieve

import android.app.Application

/**
 * Punto de inicio del proceso de Pensieve.
 *
 * Android crea esta clase antes que la actividad. El contenedor se inicializa
 * solo cuando alguien lo necesita, evitando trabajo innecesario al arrancar.
 */
class PensieveApplication : Application() {

    val appContainer: AppContainer by lazy {
        AppContainer(applicationContext)
    }
}
