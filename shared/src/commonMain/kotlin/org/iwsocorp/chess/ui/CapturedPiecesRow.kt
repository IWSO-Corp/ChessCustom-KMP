package org.iwsocorp.chess.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import org.iwsocorp.chess.engine.Piece
import org.iwsocorp.chess.engine.PieceColor

// ─── Captured Pieces Panel ───────────────────────────────────────────────────

@Composable
fun CapturedPiecesRow(
    label: String,
    pieces: List<Piece>,
    isCurrentTurn: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Turn indicator dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .padding(end = 0.dp)
        ) {
            if (isCurrentTurn) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {}
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrentTurn) androidx.compose.ui.text.font.FontWeight.Bold
            else androidx.compose.ui.text.font.FontWeight.Normal,
            modifier = Modifier.width(50.dp)
        )
        // Captured pieces
        Row {
            pieces.sortedBy { it.type.ordinal }.forEach { piece ->
                Text(
                    text = piece.unicode,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
