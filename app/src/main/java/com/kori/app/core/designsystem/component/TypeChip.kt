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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.KoriSurfaceVariant
import com.kori.app.core.designsystem.KoriTheme
import com.kori.app.core.model.transaction.TransactionType
import com.kori.app.core.ui.displayLabel

@Composable
fun TypeChip(
    type: TransactionType,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color = KoriSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(KoriPrimary.copy(alpha = 0.72f)),
        )

        Text(
            text = type.displayLabel(resources),
            style = MaterialTheme.typography.labelMedium,
            color = KoriPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TypeChipPreview() {
    KoriTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TypeChip(type = TransactionType.CLIENT_TRANSFER)
            TypeChip(type = TransactionType.CARD_PAYMENT)
            TypeChip(type = TransactionType.MERCHANT_WITHDRAW)
        }
    }
}
