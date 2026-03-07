package com.kori.app.core.designsystem.skeletons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FinancialFormSkeleton(
    modifier: Modifier = Modifier,
    extraFields: Int = 2,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonBlock(height = 20.dp, width = 150.dp)
            SkeletonBlock(height = 56.dp)
            repeat(extraFields) {
                SkeletonBlock(height = 56.dp)
            }
            SkeletonBlock(height = 48.dp)
        }
    }
}