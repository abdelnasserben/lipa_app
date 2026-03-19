package com.kori.app.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.R
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.auth.AuthState

@Composable
fun AuthWelcomeScreen(
    role: UserRole,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.lipa_wordmark),
                contentDescription = "Logo Kori",
                modifier = Modifier.width(200.dp).padding(bottom = 24.dp),
            )

            Text(
                text = "Connexion sécurisée",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = KoriPrimary,
            )

            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Sécurité",
                modifier = Modifier.size(64.dp).padding(bottom = 24.dp),
                tint = KoriAccent,
            )

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KoriAccent,
                    contentColor = KoriPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Text("Se connecter")
            }
        }
    }
}

@Composable
fun AuthCallbackScreen(
    authState: AuthState,
    onSuccess: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (authState) {
            AuthState.Unauthenticated,
            AuthState.Authenticating -> {
                CircularProgressIndicator()
                Text(
                    text = "Traitement sécurisé de votre session…",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Validation du code d’autorisation et finalisation de la session.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is AuthState.Error -> {
                Text(
                    text = "Connexion interrompue",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = authState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    onClick = onRetry,
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text("Réessayer")
                }
            }

            is AuthState.Authenticated -> {
                CircularProgressIndicator()
                Text(
                    text = "Finalisation…",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
fun AuthSuccessScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = KoriAccent,
        )

        Text(
            text = "Connexion réussie",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = KoriPrimary,
        )

        Text(
            text = "Votre session KORI est active. Vous pouvez maintenant accéder à votre dashboard.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = KoriAccent,
                contentColor = KoriPrimary,
            ),
        ) {
            Text("Accéder au dashboard")
        }
    }
}

@Composable
private fun FeatureLine(
    icon: @Composable () -> Unit,
    text: String,
) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        icon()
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
