package com.kori.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriBorder
import com.kori.app.core.designsystem.KoriPrimary

@Composable
fun TimelineStepRow(
    title: String,
    isCompleted: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(14.dp)
                    .background(
                        color = if (isCompleted) KoriAccent else KoriBorder,
                        shape = CircleShape,
                    ),
            )

            if (!isLast) {
                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                        .height(30.dp)
                        .background(
                            color = if (isCompleted) KoriAccent else KoriBorder,
                        ),
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = KoriPrimary,
            )
            Text(
                text = if (isCompleted) "Étape validée" else "En attente",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}