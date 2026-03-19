package com.kori.app.core.designsystem.component

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kori.app.R
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.model.UserRole
import com.kori.app.core.ui.actionLabelResId
import com.kori.app.core.ui.historyLabelResId
import com.kori.app.navigation.KoriDestination

@Composable
fun KoriBottomBar(
    currentRoute: String?,
    role: UserRole,
    onNavigate: (String) -> Unit,
) {
    val items = listOf(
        Triple(KoriDestination.Dashboard.route, stringResource(R.string.nav_home), Icons.Outlined.Dashboard),
        Triple(KoriDestination.Transactions.route, stringResource(R.string.nav_transactions), Icons.Outlined.Wallet),
        Triple(KoriDestination.Action.route, stringResource(role.actionLabelResId()), Icons.Outlined.SwapHoriz),
        Triple(KoriDestination.Activity.route, stringResource(role.historyLabelResId()), Icons.Outlined.History),
        Triple(KoriDestination.Profile.route, stringResource(R.string.nav_profile), Icons.Outlined.AccountCircle),
    )

    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        items.forEach { (route, label, icon) ->
            val selected = currentRoute == route

            if (route == KoriDestination.Action.route) {
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(route) },
                    icon = {
                        Surface(
                            color = KoriAccent,
                            shape = CircleShape,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.padding(10.dp),
                                tint = MaterialTheme.colorScheme.onSecondary,
                            )
                        }
                    },
                    label = { Text(label) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            } else {
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(route) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                        )
                    },
                    label = { Text(label) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )
            }
        }
    }
}
