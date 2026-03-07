package com.kori.app.core.designsystem.skeletons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardSkeleton(
    modifier: Modifier = Modifier,
    showQuickActions: Boolean = true,
    showSecondarySection: Boolean = true,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBlock(height = 28.dp, width = 180.dp)
                SkeletonBlock(height = 16.dp, width = 240.dp, cornerRadius = 10.dp)
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SkeletonBlock(height = 18.dp, width = 100.dp)
                    SkeletonBlock(height = 36.dp, width = 180.dp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SkeletonBlock(modifier = Modifier.weight(1f), height = 64.dp)
                        SkeletonBlock(modifier = Modifier.weight(1f), height = 64.dp)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBlock(modifier = Modifier.weight(1f), height = 88.dp)
                SkeletonBlock(modifier = Modifier.weight(1f), height = 88.dp)
            }
        }

        if (showQuickActions) {
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SkeletonBlock(height = 20.dp, width = 120.dp)
                        repeat(2) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                repeat(2) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        SkeletonBlock(height = 52.dp)
                                        SkeletonBlock(height = 14.dp, width = 60.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SkeletonBlock(height = 20.dp, width = 140.dp)
                    repeat(4) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SkeletonBlock(height = 44.dp, width = 44.dp, cornerRadius = 22.dp)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SkeletonBlock(height = 16.dp, width = 140.dp)
                                SkeletonBlock(height = 14.dp, width = 90.dp)
                            }
                            SkeletonBlock(height = 18.dp, width = 80.dp)
                        }
                    }
                }
            }
        }

        if (showSecondarySection) {
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SkeletonBlock(height = 20.dp, width = 160.dp)
                        repeat(3) {
                            SkeletonBlock(height = 56.dp)
                        }
                    }
                }
            }
        }
    }
}