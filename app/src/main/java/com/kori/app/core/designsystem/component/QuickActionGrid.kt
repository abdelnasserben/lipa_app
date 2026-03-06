package com.kori.app.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.KoriSurface

enum class QuickActionIcon {
    SEND,
    HISTORY,
    CARD,
    TRANSFER,
    WALLET,
}

data class QuickActionItem(
    val title: String,
    val icon: QuickActionIcon,
    val onClick: () -> Unit,
)

@Composable
fun QuickActionGrid(
    items: List<QuickActionItem>,
    modifier: Modifier = Modifier,
) {
    val rows = items.chunked(3)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowItems.forEach { item ->
                    QuickActionCard(
                        item = item,
                        modifier = Modifier.weight(1f),
                    )
                }

                repeat(3 - rowItems.size) {
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    item: QuickActionItem,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(0.dp)
            .clickable(onClick = item.onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .size(width = 0.dp, height = 110.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = when (item.icon) {
                    QuickActionIcon.SEND -> Icons.Outlined.Send
                    QuickActionIcon.HISTORY -> Icons.Outlined.History
                    QuickActionIcon.CARD -> Icons.Outlined.CreditCard
                    QuickActionIcon.TRANSFER -> Icons.Outlined.SwapHoriz
                    QuickActionIcon.WALLET -> Icons.Outlined.Wallet
                },
                contentDescription = item.title,
                tint = KoriAccent,
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(10.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = KoriPrimary,
            )
        }
    }
}