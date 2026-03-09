package com.kori.app.feature.agentsearch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriSurface
import com.kori.app.core.designsystem.component.EmptyState
import com.kori.app.core.model.search.AgentSearchItem

@Composable
fun AgentSearchScreen(
    uiState: AgentSearchUiState,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Recherche agent",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Téléphone, carte ou terminal") },
                )
                Button(
                    onClick = { onSearch(query) },
                    enabled = query.isNotBlank(),
                ) {
                    Text("Rechercher")
                }
            }
        }

        when (uiState) {
            AgentSearchUiState.Idle -> {
                item {
                    EmptyState(
                        title = "Lancez une recherche",
                        message = "Saisissez une valeur puis appuyez sur Rechercher.",
                    )
                }
            }

            AgentSearchUiState.Loading -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is AgentSearchUiState.Content -> {
                items(
                    items = uiState.items,
                    key = { it.entityRef },
                ) { item ->
                    SearchResultCard(item = item)
                }
            }

            is AgentSearchUiState.Empty -> {
                item {
                    EmptyState(
                        title = "Aucun résultat",
                        message = "Aucun élément ne correspond à “${uiState.query}”.",
                    )
                }
            }

            is AgentSearchUiState.Error -> {
                item {
                    EmptyState(
                        title = "Recherche indisponible",
                        message = uiState.message,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(item: AgentSearchItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = item.entityType,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = item.display,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            item.status?.takeIf { it.isNotBlank() }?.let { status ->
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
