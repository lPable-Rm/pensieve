package com.mvppostit.pensieve.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.ui.theme.PensieveTheme

private const val ONBOARDING_PAGE_COUNT = 2

/**
 * Presenta las dos páginas del onboarding sin decidir cuándo termina.
 *
 * La pantalla conserva temporalmente la página visible y expone callbacks para
 * que el bloque 9.1C pueda conectar DataStore y el widget sin mezclar esa lógica
 * con el diseño visual.
 */
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    canAddWidget: Boolean = true,
    onSkip: () -> Unit = {},
    onNext: () -> Unit = {},
    onStart: () -> Unit = {},
    onAddWidget: () -> Unit = {},
) {
    // rememberSaveable conserva la página durante una rotación sin crear un
    // ViewModel para un estado puramente visual y de vida muy corta.
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    // Ambas páginas pueden necesitar desplazamiento cuando aumenta el tamaño
    // del texto. Al cambiar de página cada una debe empezar siempre arriba.
    LaunchedEffect(currentPage) {
        scrollState.scrollTo(0)
    }

    val isFirstPage = currentPage == 0
    val pageDescription = stringResource(
        R.string.onboarding_page_indicator,
        currentPage + 1,
        ONBOARDING_PAGE_COUNT,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // El fondo llega a los bordes, pero ninguna acción invade barras
            // del sistema o recortes de pantalla.
            .safeDrawingPadding()
            // El scroll permite usar la pantalla con texto grande o poco alto.
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onSkip,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ),
            ) {
                Text(text = stringResource(R.string.onboarding_skip))
            }
        }

        OnboardingPageContent(currentPage = currentPage)

        Spacer(modifier = Modifier.height(24.dp))

        PageIndicator(
            currentPage = currentPage,
            contentDescription = pageDescription,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            onClick = {
                if (isFirstPage) {
                    // El avance es parte del componente visual; la finalización
                    // del onboarding sigue siendo responsabilidad del bloque 9.1C.
                    currentPage = 1
                    onNext()
                } else if (canAddWidget) {
                    onAddWidget()
                } else {
                    onStart()
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            val label = when {
                isFirstPage -> R.string.onboarding_next
                canAddWidget -> R.string.onboarding_add_widget
                else -> R.string.onboarding_start
            }
            Text(text = stringResource(label))
        }
    }
}

/** Los puntos son simples indicadores, no controles de navegación. */
@Composable
private fun PageIndicator(
    currentPage: Int,
    contentDescription: String,
) {
    Row(
        modifier = Modifier.semantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(ONBOARDING_PAGE_COUNT) { page ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (page == currentPage) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Preview(
    name = "Onboarding con barras del sistema",
    showBackground = true,
    showSystemUi = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun OnboardingScreenPreview() {
    PensieveTheme {
        OnboardingScreen()
    }
}

@Preview(
    name = "Onboarding compacto con texto grande",
    showBackground = true,
    widthDp = 360,
    heightDp = 640,
    fontScale = 1.8f,
)
@Composable
private fun OnboardingScreenCompactPreview() {
    PensieveTheme {
        OnboardingScreen()
    }
}
