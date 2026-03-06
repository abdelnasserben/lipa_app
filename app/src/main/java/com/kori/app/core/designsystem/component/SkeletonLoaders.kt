package com.kori.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriSurface
import com.kori.app.core.designsystem.KoriSurfaceVariant

@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .background(
                color = KoriSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
            ),
    )
}

@Composable
fun SkeletonBalanceCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SkeletonBlock(
                    modifier = Modifier
                        .width(120.dp)
                        .height(18.dp),
                )
                SkeletonBlock(
                    modifier = Modifier
                        .width(180.dp)
                        .height(32.dp),
                )
                SkeletonBlock(
                    modifier = Modifier
                        .width(140.dp)
                        .height(18.dp),
                )
            }
        }
    }
}

@Composable
fun SkeletonTransactionRow(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SkeletonBlock(
                    modifier = Modifier
                        .width(110.dp)
                        .height(24.dp),
                )
                SkeletonBlock(
                    modifier = Modifier
                        .width(90.dp)
                        .height(24.dp),
                )
            }

            SkeletonBlock(
                modifier = Modifier
                    .width(150.dp)
                    .height(20.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SkeletonBlock(
                    modifier = Modifier
                        .width(100.dp)
                        .height(14.dp),
                )
                SkeletonBlock(
                    modifier = Modifier
                        .width(90.dp)
                        .height(14.dp),
                )
            }
        }
    }
}

@Composable
fun SkeletonInfoCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonBlock(
                modifier = Modifier
                    .width(140.dp)
                    .height(18.dp),
            )
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp),
            )
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp),
            )
        }
    }
}