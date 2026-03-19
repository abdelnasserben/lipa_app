package com.kori.app.feature.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.R
import com.kori.app.core.designsystem.component.ClientCardListItem
import com.kori.app.core.designsystem.component.EmptyState
import com.kori.app.core.designsystem.component.ErrorState
import com.kori.app.core.model.dashboard.CardItem

@Composable
fun ClientCardsScreen(
    uiState: ClientCardsUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        ClientCardsUiState.Loading -> ClientCardsLoading(modifier = modifier)
        ClientCardsUiState.Empty -> ClientCardsEmpty(modifier = modifier)
        is ClientCardsUiState.Error -> ClientCardsError(
            message = uiState.message,
            onRetry = onRetry,
            modifier = modifier,
        )

        is ClientCardsUiState.Content -> ClientCardsContent(
            cards = uiState.cards,
            modifier = modifier,
        )
    }
}

@Composable
private fun ClientCardsLoading(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.client_cards_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.client_cards_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ClientCardsEmpty(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
    ) {
        EmptyState(
            title = stringResource(R.string.client_cards_empty_title),
            message = stringResource(R.string.client_cards_empty_message),
        )
    }
}

@Composable
private fun ClientCardsError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
    ) {
        ErrorState(
            title = stringResource(R.string.client_cards_error_title),
            message = message,
            onRetry = onRetry,
        )
    }
}

@Composable
private fun ClientCardsContent(
    cards: List<CardItem>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.client_cards_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.client_cards_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(cards.size) { index ->
            ClientCardListItem(
                card = cards[index],
                index = index + 1,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
