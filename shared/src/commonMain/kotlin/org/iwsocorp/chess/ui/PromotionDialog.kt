package org.iwsocorp.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import org.iwsocorp.chess.engine.Piece
import org.iwsocorp.chess.engine.PieceColor
import org.iwsocorp.chess.engine.PieceType
import org.iwsocorp.chess.viewmodel.PendingPromotion

@Composable
fun PromotionDialog(
    pending: PendingPromotion,
    onPieceSelected: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
    val promotionPieces =
        listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Promosi Pion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Pilih bidak untuk promosi",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    promotionPieces.forEach { pieceType ->
                        PromotionPieceButton(
                            piece = Piece(pieceType, pending.color),
                            onClick = { onPieceSelected(pieceType) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PromotionPieceButton(
    piece: Piece,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = piece.unicode,
            fontSize = 36.sp,
            textAlign = TextAlign.Center
        )
    }
}
