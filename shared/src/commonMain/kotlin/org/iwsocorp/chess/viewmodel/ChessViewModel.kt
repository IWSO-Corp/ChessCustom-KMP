package org.iwsocorp.chess.viewmodel

import androidx.lifecycle.ViewModel
import org.iwsocorp.chess.engine.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ─── UI State ────────────────────────────────────────────────────────────────

data class ChessUiState(
    val gameState: GameState = GameState(),
    val selectedSquare: Square? = null,
    val validMoves: List<Move> = emptyList(),
    val pendingPromotion: PendingPromotion? = null,
    val lastMove: Move? = null,
    val showResetDialog: Boolean = false
)

data class PendingPromotion(
    val from: Square,
    val to: Square,
    val color: PieceColor
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

class ChessViewModel : ViewModel() {

    private val game = ChessGame()
    private val _uiState = MutableStateFlow(ChessUiState(gameState = game.state))
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    // ── Square selection / move ──────────────────────────────────────────────

    fun onSquareTapped(square: Square) {
        val state = _uiState.value

        // If promotion dialog is showing, ignore board taps
        if (state.pendingPromotion != null) return

        // If game is over, ignore
        if (state.gameState.isGameOver) return

        val piece = state.gameState.board.pieceAt(square)
        val currentTurn = state.gameState.currentTurn

        // Case 1: Nothing selected yet — select own piece
        if (state.selectedSquare == null) {
            if (piece?.color == currentTurn) {
                selectSquare(square)
            }
            return
        }

        // Case 2: Something already selected
        val selectedSq = state.selectedSquare

        // Tapped same square — deselect
        if (selectedSq == square) {
            deselect()
            return
        }

        // Tapped another own piece — switch selection
        if (piece?.color == currentTurn) {
            selectSquare(square)
            return
        }

        // Try to move
        val validMove = state.validMoves.firstOrNull { it.to == square }
        if (validMove != null) {
            // Check if promotion required
            if (game.isPromotionMove(selectedSq, square)) {
                _uiState.update {
                    it.copy(
                        pendingPromotion = PendingPromotion(selectedSq, square, currentTurn),
                        selectedSquare = null,
                        validMoves = emptyList()
                    )
                }
            } else {
                applyMove(validMove)
            }
        } else {
            deselect()
        }
    }

    fun onPromotionSelected(pieceType: PieceType) {
        val pending = _uiState.value.pendingPromotion ?: return
        val piece = game.state.board.pieceAt(pending.from) ?: return
        val move = Move(
            from = pending.from,
            to = pending.to,
            piece = piece,
            promotionType = pieceType
        )
        _uiState.update { it.copy(pendingPromotion = null) }
        applyMove(move)
    }

    fun onPromotionDismissed() {
        _uiState.update { it.copy(pendingPromotion = null) }
    }

    // ── Actions ─────────────────────────────────────────────────────────────

    fun onUndoClicked() {
        if (game.undo()) {
            _uiState.update {
                it.copy(
                    gameState = game.state,
                    selectedSquare = null,
                    validMoves = emptyList(),
                    lastMove = game.state.lastMove,
                    pendingPromotion = null
                )
            }
        }
    }

    fun onResetClicked() {
        _uiState.update { it.copy(showResetDialog = true) }
    }

    fun onResetConfirmed() {
        game.reset()
        _uiState.value = ChessUiState(gameState = game.state)
    }

    fun onResetDismissed() {
        _uiState.update { it.copy(showResetDialog = false) }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun selectSquare(square: Square) {
        val moves = game.getLegalMoves(square)
        _uiState.update {
            it.copy(selectedSquare = square, validMoves = moves)
        }
    }

    private fun deselect() {
        _uiState.update { it.copy(selectedSquare = null, validMoves = emptyList()) }
    }

    private fun applyMove(move: Move) {
        game.makeMove(move)
        _uiState.update {
            it.copy(
                gameState = game.state,
                selectedSquare = null,
                validMoves = emptyList(),
                lastMove = move
            )
        }
    }
}
