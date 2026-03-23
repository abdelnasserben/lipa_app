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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.R
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.KoriSurface
import com.kori.app.core.designsystem.component.ErrorState
import com.kori.app.core.model.UserRole
import com.kori.app.core.ui.formatIsoToDisplay
import com.kori.app.core.ui.labelResId

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current

    when (uiState) {
        ProfileUiState.Loading -> LoadingContent(modifier = modifier)
        is ProfileUiState.Error -> {
            ErrorContent(
                message = uiState.message,
                settings = uiState.settings,
                onRetry = onRetry,
                onLogout = onLogout,
                onLanguageSelected = onLanguageSelected,
                onNotificationsChanged = onNotificationsChanged,
                modifier = modifier,
            )
        }

        is ProfileUiState.Content -> {
            ProfileContent(
                state = uiState,
                onLogout = onLogout,
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
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        item { SkeletonCard(title = stringResource(R.string.profile_loading_title)) }
        item { SkeletonCard(title = stringResource(R.string.profile_loading_settings)) }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    settings: ProfileSettingsUiModel,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
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
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        item {
            ErrorState(
                title = stringResource(R.string.profile_error_title),
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

        item {
            LogoutButton(
                onLogout = onLogout,
            )
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState.Content,
    onLogout: () -> Unit,
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
        item { Header(role = state.role) }

        item {
            ProfileCard(
                profile = state.profile,
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
            LogoutButton(
                onLogout = onLogout,
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
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = KoriPrimary,
        )
        Text(
            text = stringResource(R.string.profile_header_subtitle, stringResource(role.labelResId()).lowercase()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileCard(
    profile: ProfileCardUiModel,
) {
    val resources = LocalResources.current

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

            ProfileLine(stringResource(R.string.profile_code), profile.code)
            ProfileLine(stringResource(R.string.common_status), profile.status)
            ProfileLine(stringResource(R.string.profile_created_at), formatIsoToDisplay(resources, profile.createdAt))

            profile.phone?.let { ProfileLine(stringResource(R.string.profile_phone), it) }
        }
    }
}

@Composable
private fun LogoutButton(
    onLogout: () -> Unit,
) {
    Button(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = KoriAccent,
            contentColor = KoriPrimary,
        ),
    ) {
        Text(stringResource(R.string.profile_logout))
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
                text = stringResource(R.string.profile_settings_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.profile_language),
                    style = MaterialTheme.typography.labelLarge,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AppLanguage.entries) { language ->
                        FilterChip(
                            selected = language == settings.language,
                            onClick = { onLanguageSelected(language) },
                            label = { Text(stringResource(language.labelResId)) },
                        )
                    }
                }
            }

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.profile_theme),
                    style = MaterialTheme.typography.labelLarge,
                )
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(settings.themeMode.labelResId)) },
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
                        text = stringResource(R.string.profile_notifications),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = stringResource(R.string.profile_notifications_helper),
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
                text = stringResource(R.string.profile_preview_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = stringResource(R.string.profile_preview_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(UserRole.entries) { role ->
                    FilterChip(
                        selected = role == activeRole,
                        onClick = { onSelectRole(role) },
                        label = { Text(stringResource(role.labelResId())) },
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                text = stringResource(R.string.profile_loading_wait),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
