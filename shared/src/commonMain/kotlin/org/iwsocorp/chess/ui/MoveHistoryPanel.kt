package org.iwsocorp.chess.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import org.iwsocorp.chess.engine.Move

// ─── Move History ────────────────────────────────────────────────────────────

@Composable
fun MoveHistoryPanel(
    moves: List<Move>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to latest move
    LaunchedEffect(moves.size) {
        if (moves.isNotEmpty()) {
            listState.animateScrollToItem((moves.size - 1) / 2)
        }
    }

    Column(modifier = modifier) {
        Text(
            text = "Riwayat Gerakan",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (moves.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Belum ada gerakan",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        } else {
            // Group moves into pairs (white + black)
            val movePairs = moves.chunked(2)
            LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(movePairs) { index, pair ->
                    MoveRow(
                        moveNumber = index + 1,
                        whiteMove = pair.getOrNull(0),
                        blackMove = pair.getOrNull(1),
                        isLatest = index == movePairs.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun MoveRow(
    moveNumber: Int,
    whiteMove: Move?,
    blackMove: Move?,
    isLatest: Boolean
) {
    val bgColor = if (isLatest) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$moveNumber.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.width(28.dp)
        )
        Text(
            text = whiteMove?.displayNotation ?: "",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = blackMove?.displayNotation ?: "",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}
