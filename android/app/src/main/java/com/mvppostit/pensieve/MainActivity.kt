package com.mvppostit.pensieve

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mvppostit.pensieve.ui.home.HomeRoute
import com.mvppostit.pensieve.ui.theme.PensieveTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // La Application conserva las dependencias durante todo el proceso.
        // MainActivity solo entrega a la ruta la dependencia que esta necesita.
        val reminderManager = (application as PensieveApplication)
            .appContainer
            .reminderManager

        setContent {
            PensieveTheme {
                // Estado compartido entre el Scaffold, que dibuja el snackbar,
                // y HomeScreen, que más adelante pedirá mostrar mensajes.
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    // Scaffold coloca el snackbar sobre el contenido y lo mantiene
                    // visible durante el tiempo que indique SnackbarHostState.
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                ) { innerPadding ->
                    HomeRoute(
                        reminderManager = reminderManager,
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
