package com.kori.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriError
import com.kori.app.core.designsystem.KoriSurfaceVariant
import com.kori.app.core.designsystem.KoriSuccess
import com.kori.app.core.designsystem.KoriWarning
import com.kori.app.core.model.transaction.TransactionStatus

@Composable
fun StatusBadge(
    status: TransactionStatus,
    modifier: Modifier = Modifier,
) {
    val background = when (status) {
        TransactionStatus.COMPLETED -> KoriSuccess.copy(alpha = 0.12f)
        TransactionStatus.PENDING -> KoriWarning.copy(alpha = 0.16f)
        TransactionStatus.FAILED -> KoriError.copy(alpha = 0.12f)
        TransactionStatus.REVERSED -> KoriSurfaceVariant
    }

    val textColor = when (status) {
        TransactionStatus.COMPLETED -> KoriSuccess
        TransactionStatus.PENDING -> KoriWarning
        TransactionStatus.FAILED -> KoriError
        TransactionStatus.REVERSED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = status.name,
        modifier = modifier
            .background(
                color = background,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
    )
}