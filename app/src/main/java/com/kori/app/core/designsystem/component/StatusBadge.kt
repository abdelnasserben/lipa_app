package com.kori.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriError
import com.kori.app.core.designsystem.KoriSurfaceVariant
import com.kori.app.core.designsystem.KoriTheme
import com.kori.app.core.designsystem.KoriSuccess
import com.kori.app.core.designsystem.KoriWarning
import com.kori.app.core.model.transaction.TransactionStatus
import com.kori.app.core.ui.displayLabel

@Composable
fun StatusBadge(
    status: TransactionStatus,
    modifier: Modifier = Modifier,
) {
    val background = when (status) {
        TransactionStatus.COMPLETED -> KoriSuccess.copy(alpha = 0.14f)
        TransactionStatus.PENDING -> KoriWarning.copy(alpha = 0.18f)
        TransactionStatus.FAILED -> KoriError.copy(alpha = 0.14f)
        TransactionStatus.REVERSED -> KoriSurfaceVariant
    }

    val textColor = when (status) {
        TransactionStatus.COMPLETED -> KoriSuccess
        TransactionStatus.PENDING -> KoriWarning
        TransactionStatus.FAILED -> KoriError
        TransactionStatus.REVERSED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color = background)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor),
        )

        Text(
            text = status.displayLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusBadgePreview() {
    KoriTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(status = TransactionStatus.COMPLETED)
            StatusBadge(status = TransactionStatus.PENDING)
            StatusBadge(status = TransactionStatus.FAILED)
            StatusBadge(status = TransactionStatus.REVERSED)
        }
    }
}
