package com.kori.app.core.designsystem.skeletons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DetailsScreenSkeleton(
    modifier: Modifier = Modifier,
    detailRows: Int = 6,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonBlock(height = 28.dp, width = 180.dp)
        SkeletonBlock(height = 16.dp, width = 240.dp)

        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonBlock(height = 18.dp, width = 120.dp)
                SkeletonBlock(height = 34.dp, width = 150.dp)
                SkeletonBlock(height = 24.dp, width = 100.dp, cornerRadius = 12.dp)
            }
        }

        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                repeat(detailRows) {
                    SkeletonBlock(height = 20.dp)
                }
            }
        }
    }
}