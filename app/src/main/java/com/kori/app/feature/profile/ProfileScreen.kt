package com.kori.app.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.KoriSurface
import com.kori.app.core.designsystem.component.ErrorState
import com.kori.app.core.model.UserRole
import com.kori.app.core.ui.formatIsoToDisplay

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onRetry: () -> Unit,
    onOpenSession: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        ProfileUiState.Loading -> {
            LoadingContent(modifier = modifier)
        }

        is ProfileUiState.Error -> {
            ErrorContent(
                message = uiState.message,
                settings = uiState.settings,
                onRetry = onRetry,
                onLanguageSelected = onLanguageSelected,
                onNotificationsChanged = onNotificationsChanged,
                modifier = modifier,
            )
        }

        is ProfileUiState.Content -> {
            ProfileContent(
                state = uiState,
                onOpenSession = onOpenSession,
                onLanguageSelected = onLanguageSelected,
                onNotificationsChanged = onNotificationsChanged,
                onSelectRole = onSelectRole,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Profil",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        item {
            SkeletonCard(title = "Chargement du profil…")
        }

        item {
            SkeletonCard(title = "Préparation des paramètres…")
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    settings: ProfileSettingsUiModel,
    onRetry: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Profil",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        item {
            ErrorState(
                title = "Profil indisponible",
                message = message,
                onRetry = onRetry,
            )
        }

        item {
            SettingsCard(
                settings = settings,
                onLanguageSelected = onLanguageSelected,
                onNotificationsChanged = onNotificationsChanged,
            )
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState.Content,
    onOpenSession: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Header(role = state.role)
        }

        item {
            ProfileCard(
                profile = state.profile,
            )
        }

        item {
            SessionCard(
                onOpenSession = onOpenSession,
            )
        }

        item {
            SettingsCard(
                settings = state.settings,
                onLanguageSelected = onLanguageSelected,
                onNotificationsChanged = onNotificationsChanged,
            )
        }

        item {
            DevMenuCard(
                activeRole = state.role,
                onSelectRole = onSelectRole,
            )
        }
    }
}

@Composable
private fun Header(
    role: UserRole,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Profil",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = KoriPrimary,
        )
        Text(
            text = "Espace ${role.label.lowercase()} • paramètres, session et outils de prévisualisation.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileCard(
    profile: ProfileCardUiModel,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = profile.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            ProfileLine("Code", profile.code)
            ProfileLine("Statut", profile.status)
            ProfileLine("Créé le", formatIsoToDisplay(profile.createdAt))

            profile.phone?.let {
                ProfileLine("Téléphone", it)
            }
        }
    }
}

@Composable
private fun SessionCard(
    onOpenSession: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Session",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Consultez les informations de session OIDC mockées, les tokens masqués et l’expiration ISO-8601.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = onOpenSession,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KoriAccent,
                    contentColor = KoriPrimary,
                ),
            ) {
                Text("Ouvrir la session OIDC")
            }
        }
    }
}

@Composable
private fun SettingsCard(
    settings: ProfileSettingsUiModel,
    onLanguageSelected: (AppLanguage) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Paramètres",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Langue",
                    style = MaterialTheme.typography.labelLarge,
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(AppLanguage.entries) { language ->
                        FilterChip(
                            selected = language == settings.language,
                            onClick = { onLanguageSelected(language) },
                            label = { Text(language.label) },
                        )
                    }
                }
            }

            HorizontalDivider()

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Thème",
                    style = MaterialTheme.typography.labelLarge,
                )
                AssistChip(
                    onClick = {},
                    label = { Text(settings.themeMode.label) },
                    colors = AssistChipDefaults.assistChipColors(),
                    shape = RoundedCornerShape(999.dp),
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = "Préférence locale mockée pour les alertes et rappels.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Switch(
                    checked = settings.notificationsEnabled,
                    onCheckedChange = onNotificationsChanged,
                )
            }
        }
    }
}

@Composable
private fun DevMenuCard(
    activeRole: UserRole,
    onSelectRole: (UserRole) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Dev menu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "Changez rapidement de rôle pour prévisualiser les expériences Client, Merchant et Agent.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(UserRole.entries) { role ->
                    FilterChip(
                        selected = role == activeRole,
                        onClick = { onSelectRole(role) },
                        label = { Text(role.label) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileLine(
    label: String,
    value: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun SkeletonCard(
    title: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(min = 200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Veuillez patienter…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}