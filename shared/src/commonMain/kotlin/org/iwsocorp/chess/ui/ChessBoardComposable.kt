package org.iwsocorp.chess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import org.iwsocorp.chess.engine.*
import org.iwsocorp.chess.viewmodel.ChessUiState

// ─── Colors ──────────────────────────────────────────────────────────────────

private val LightSquare = Color(0xFFF0D9B5)
private val DarkSquare = Color(0xFFB58863)
private val SelectedTint = Color(0xCCF6F669)
private val ValidMoveDot = Color(0x44000000)
private val LastMoveTint = Color(0x88CDD16E)
private val CheckTint = Color(0xAAEB6050)
private val CaptureRing = Color(0x88EB6050)

// ─── Board Composable ─────────────────────────────────────────────────────────

@Composable
fun ChessBoard(
    uiState: ChessUiState,
    onSquareTapped: (Square) -> Unit,
    modifier: Modifier = Modifier
) {
    val board = uiState.gameState.board
    val selected = uiState.selectedSquare
    val validMoves = uiState.validMoves
    val lastMove = uiState.lastMove
    val status = uiState.gameState.status

    // King in check square
    val checkKingSq = if (status is GameStatus.Check) board.findKing(status.color) else null
    val checkmateKingSq =
        if (status is GameStatus.Checkmate) board.findKing(status.winner.opponent()) else null
    val highlightCheckSq = checkKingSq ?: checkmateKingSq

    BoxWithConstraints(modifier = modifier) {
        val boardSize = minOf(maxWidth, maxHeight)
        val cellSize = boardSize / 9f  // 8 cells + coordinate labels

        val squarePx = (boardSize - cellSize) / 7

        Column {
            Row {
                // Rank labels (left)
//                Column(modifier = Modifier.width(cellSize)) {
//                    for (row in 0..7) {
//                        Box(
//                            modifier = Modifier.size(squarePx),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = "${8 - row}",
//                                fontSize = 10.sp,
//                                color = Color.Gray,
//                                textAlign = TextAlign.Center
//                            )
//                        }
//                    }
//                }

                // Board grid
                Column(
                    modifier = Modifier
                        .size(squarePx * 8)
                        .border(2.dp, Color(0xFF8B7355), RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    for (row in 0..7) {
                        Row {
                            for (col in 0..7) {
                                val sq = Square(row, col)
                                val piece = board.pieceAt(sq)
                                val isLight = (row + col) % 2 == 0
                                val isSelected = selected == sq
                                val isLastMoveFrom = lastMove?.from == sq
                                val isLastMoveTo = lastMove?.to == sq
                                val isValidTarget = validMoves.any { it.to == sq }
                                val isCapture =
                                    isValidTarget && piece != null && piece.color != uiState.gameState.currentTurn
                                val isCheck = highlightCheckSq == sq

                                val baseColor = if (isLight) LightSquare else DarkSquare
                                val bgColor = when {
                                    isSelected -> SelectedTint
                                    isCheck -> CheckTint
                                    isLastMoveFrom || isLastMoveTo -> LastMoveTint
                                    else -> baseColor
                                }

                                Box(
                                    modifier = Modifier
                                        .size(squarePx)
                                        .background(bgColor)
                                        .clickable { onSquareTapped(sq) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Valid move indicator
                                    if (isValidTarget && !isCapture) {
                                        Box(
                                            modifier = Modifier
                                                .size(squarePx * 0.32f)
                                                .background(ValidMoveDot, CircleShape)
                                        )
                                    }
                                    // Capture ring
                                    if (isCapture) {
                                        Box(
                                            modifier = Modifier
                                                .size(squarePx - 2.dp)
                                                .border(3.dp, CaptureRing, CircleShape)
                                        )
                                    }
                                    // Piece
                                    if (piece != null) {
                                        PieceText(
                                            piece = piece,
                                            size = squarePx * 0.78f
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // File labels (bottom)
//            Row(modifier = Modifier.padding(start = cellSize)) {
//                for (col in 0..7) {
//                    Box(
//                        modifier = Modifier.size(squarePx, cellSize),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "${'a' + col}",
//                            fontSize = 10.sp,
//                            color = Color.Gray,
//                            textAlign = TextAlign.Center
//                        )
//                    }
//                }
//            }
        }
    }
}

// ─── Piece Text ──────────────────────────────────────────────────────────────

@Composable
fun PieceText(piece: Piece, size: Dp) {
    Text(
        text = piece.unicode,
        fontSize = (size.value * 0.72f).sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.size(size)
    )
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview
@Composable
fun ChessBoardPreview() {
    MaterialTheme {
        ChessBoard(
            uiState = ChessUiState(),
            onSquareTapped = {}
        )
    }
}

@Preview
@Composable
fun ChessBoardSelectedPreview() {
    val square = Square(6, 4) // e2
    val gameState = GameState()
    val moves = gameState.board.legalMoves(square)
    MaterialTheme {
        ChessBoard(
            uiState = ChessUiState(
                gameState = gameState,
                selectedSquare = square,
                validMoves = moves
            ),
            onSquareTapped = {}
        )
    }
}

@Preview
@Composable
fun ChessBoardCheckPreview() {
    val gameState = GameState(
        status = GameStatus.Check(PieceColor.WHITE)
    )
    MaterialTheme {
        ChessBoard(
            uiState = ChessUiState(
                gameState = gameState
            ),
            onSquareTapped = {}
        )
    }
}
