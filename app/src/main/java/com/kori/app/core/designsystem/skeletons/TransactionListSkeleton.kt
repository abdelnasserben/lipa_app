package com.kori.app.core.designsystem.skeletons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TransactionListSkeleton(
    modifier: Modifier = Modifier,
    showFilters: Boolean = true,
    itemCount: Int = 6,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBlock(height = 28.dp, width = 180.dp)
                SkeletonBlock(height = 16.dp, width = 220.dp)
            }
        }

        if (showFilters) {
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SkeletonBlock(height = 20.dp, width = 110.dp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SkeletonBlock(modifier = Modifier.weight(1f), height = 40.dp)
                            SkeletonBlock(modifier = Modifier.weight(1f), height = 40.dp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SkeletonBlock(height = 32.dp, width = 72.dp)
                            SkeletonBlock(height = 32.dp, width = 88.dp)
                            SkeletonBlock(height = 32.dp, width = 96.dp)
                        }
                    }
                }
            }
        }

        items((1..itemCount).toList()) {
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SkeletonBlock(height = 44.dp, width = 44.dp, cornerRadius = 22.dp)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SkeletonBlock(height = 16.dp, width = 150.dp)
                        SkeletonBlock(height = 14.dp, width = 110.dp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SkeletonBlock(height = 24.dp, width = 72.dp, cornerRadius = 12.dp)
                            SkeletonBlock(height = 24.dp, width = 86.dp, cornerRadius = 12.dp)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonBlock(height = 18.dp, width = 84.dp)
                        SkeletonBlock(height = 14.dp, width = 64.dp)
                    }
                }
            }
        }
    }
}