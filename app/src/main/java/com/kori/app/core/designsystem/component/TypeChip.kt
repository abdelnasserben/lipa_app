package com.kori.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.KoriSurfaceVariant
import com.kori.app.core.model.transaction.TransactionType

@Composable
fun TypeChip(
    type: TransactionType,
    modifier: Modifier = Modifier,
) {
    Text(
        text = type.name,
        modifier = modifier
            .background(
                color = KoriSurfaceVariant,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelMedium,
        color = KoriPrimary,
    )
}