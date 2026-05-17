package org.iwsocorp.chess.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.iwsocorp.chess.engine.*
import org.iwsocorp.chess.viewmodel.ChessViewModel
import org.iwsocorp.chess.viewmodel.ChessUiState

// ─── Chess Screen ────────────────────────────────────────────────────────────

@Composable
fun ChessScreen(
    viewModel: ChessViewModel = viewModel { ChessViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChessScreenContent(
        uiState = uiState,
        onPromotionSelected = viewModel::onPromotionSelected,
        onPromotionDismissed = viewModel::onPromotionDismissed,
        onResetDismissed = viewModel::onResetDismissed,
        onResetConfirmed = viewModel::onResetConfirmed,
        onUndoClicked = viewModel::onUndoClicked,
        onResetClicked = viewModel::onResetClicked,
        onSquareTapped = viewModel::onSquareTapped
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChessScreenContent(
    uiState: ChessUiState,
    onPromotionSelected: (PieceType) -> Unit,
    onPromotionDismissed: () -> Unit,
    onResetDismissed: () -> Unit,
    onResetConfirmed: () -> Unit,
    onUndoClicked: () -> Unit,
    onResetClicked: () -> Unit,
    onSquareTapped: (Square) -> Unit
) {
    // Promotion dialog
    uiState.pendingPromotion?.let { pending ->
        PromotionDialog(
            pending = pending,
            onPieceSelected = onPromotionSelected,
            onDismiss = onPromotionDismissed
        )
    }

    // Reset confirmation dialog
    if (uiState.showResetDialog) {
        AlertDialog(
            onDismissRequest = onResetDismissed,
            title = { Text("Reset Permainan?") },
            text = { Text("Semua progres akan hilang.") },
            confirmButton = {
                TextButton(onClick = onResetConfirmed) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = onResetDismissed) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("♟ Chess", fontWeight = FontWeight.Bold)
                },
                actions = {
                    // Undo
                    TextButton(
                        onClick = onUndoClicked,
                        enabled = uiState.gameState.moveHistory.isNotEmpty() && !uiState.gameState.isGameOver
                    ) {
                        Text("↩ Undo")
                    }
                    // Reset
                    IconButton(onClick = onResetClicked) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (maxWidth > maxHeight) {
                LandscapeLayout(uiState, onSquareTapped)
            } else {
                PortraitLayout(uiState, onSquareTapped)
            }
        }
    }
}

// ─── Portrait Layout ──────────────────────────────────────────────────────────

@Composable
private fun PortraitLayout(
    uiState: ChessUiState,
    onSquareTapped: (Square) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // Black player info + captured
        PlayerRow(
            label = "Hitam",
            isCurrentTurn = uiState.gameState.currentTurn == PieceColor.BLACK,
            capturedPieces = uiState.gameState.capturedByBlack,
            gameStatus = uiState.gameState.status,
            color = PieceColor.BLACK
        )

        Spacer(Modifier.height(8.dp))

        // Board
        ChessBoard(
            uiState = uiState,
            onSquareTapped = onSquareTapped,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Spacer(Modifier.height(8.dp))

        // White player info + captured
        PlayerRow(
            label = "Putih",
            isCurrentTurn = uiState.gameState.currentTurn == PieceColor.WHITE,
            capturedPieces = uiState.gameState.capturedByWhite,
            gameStatus = uiState.gameState.status,
            color = PieceColor.WHITE
        )

        Spacer(Modifier.height(12.dp))

        // Status message
        GameStatusBanner(uiState.gameState.status, uiState.gameState.currentTurn)

        Spacer(Modifier.height(12.dp))

        // Move history
        MoveHistoryPanel(
            moves = uiState.gameState.moveHistory,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp)
        )
    }
}

// ─── Landscape Layout ─────────────────────────────────────────────────────────

@Composable
private fun LandscapeLayout(
    uiState: ChessUiState,
    onSquareTapped: (Square) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Board
        ChessBoard(
            uiState = uiState,
            onSquareTapped = onSquareTapped,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
        )

        // Side panel
        Column(modifier = Modifier.weight(1f)) {
            PlayerRow(
                label = "Hitam",
                isCurrentTurn = uiState.gameState.currentTurn == PieceColor.BLACK,
                capturedPieces = uiState.gameState.capturedByBlack,
                gameStatus = uiState.gameState.status,
                color = PieceColor.BLACK
            )
            Spacer(Modifier.height(8.dp))
            GameStatusBanner(uiState.gameState.status, uiState.gameState.currentTurn)
            Spacer(Modifier.height(8.dp))
            MoveHistoryPanel(
                moves = uiState.gameState.moveHistory,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.height(8.dp))
            PlayerRow(
                label = "Putih",
                isCurrentTurn = uiState.gameState.currentTurn == PieceColor.WHITE,
                capturedPieces = uiState.gameState.capturedByWhite,
                gameStatus = uiState.gameState.status,
                color = PieceColor.WHITE
            )
        }
    }
}

// ─── Player Row ──────────────────────────────────────────────────────────────

@Composable
private fun PlayerRow(
    label: String,
    isCurrentTurn: Boolean,
    capturedPieces: List<Piece>,
    gameStatus: GameStatus,
    color: PieceColor
) {
    val isWinner = gameStatus is GameStatus.Checkmate && gameStatus.winner == color
    val isLoser = gameStatus is GameStatus.Checkmate && gameStatus.winner != color

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isWinner -> MaterialTheme.colorScheme.primaryContainer
                isCurrentTurn && !gameStatus.let { it is GameStatus.Checkmate || it is GameStatus.Stalemate } ->
                    MaterialTheme.colorScheme.secondaryContainer

                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color swatch
            Surface(
                modifier = Modifier.size(20.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (color == PieceColor.WHITE) Color.White else Color(0xFF333333),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
            ) {}
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label + if (isWinner) " 🏆 Menang!" else if (isLoser) " ❌ Kalah" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Normal
                )
                Row {
                    capturedPieces.forEach { piece ->
                        Text(text = piece.unicode, fontSize = 14.sp)
                    }
                }
            }
            if (isCurrentTurn && !gameStatus.let { it is GameStatus.Checkmate || it is GameStatus.Stalemate }) {
                Text("▶", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
        }
    }
}

// ─── Status Banner ───────────────────────────────────────────────────────────

@Composable
private fun GameStatusBanner(status: GameStatus, currentTurn: PieceColor) {
    val (message, color) = when (status) {
        is GameStatus.Ongoing -> "${currentTurn.displayName()} bergerak" to MaterialTheme.colorScheme.onSurface
        is GameStatus.Check -> "⚠️ Skak! ${status.color.displayName()} dalam bahaya" to MaterialTheme.colorScheme.error
        is GameStatus.Checkmate -> "♟ Skakmat! ${status.winner.displayName()} menang!" to MaterialTheme.colorScheme.primary
        is GameStatus.Stalemate -> "🤝 Seri — Stalemate" to MaterialTheme.colorScheme.secondary
        is GameStatus.Draw -> "🤝 Seri — 50-move rule" to MaterialTheme.colorScheme.secondary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview
@Composable
fun ChessScreenPreview() {
    MaterialTheme {
        ChessScreenContent(
            uiState = ChessUiState(),
            onPromotionSelected = {},
            onPromotionDismissed = {},
            onResetDismissed = {},
            onResetConfirmed = {},
            onUndoClicked = {},
            onResetClicked = {},
            onSquareTapped = {}
        )
    }
}
