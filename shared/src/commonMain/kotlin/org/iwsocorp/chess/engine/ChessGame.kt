package org.iwsocorp.chess.engine

// ─── Game State ──────────────────────────────────────────────────────────────

data class GameState(
    val board: ChessBoard = ChessBoard.initial(),
    val currentTurn: PieceColor = PieceColor.WHITE,
    val status: GameStatus = GameStatus.Ongoing,
    val moveHistory: List<Move> = emptyList(),
    val halfMoveClock: Int = 0,       // for 50-move rule
    val fullMoveNumber: Int = 1
) {
    val isGameOver: Boolean get() = status !is GameStatus.Ongoing && status !is GameStatus.Check
    val lastMove: Move? get() = moveHistory.lastOrNull()

    val capturedByWhite: List<Piece> get() = board.capturedBy(PieceColor.WHITE)
    val capturedByBlack: List<Piece> get() = board.capturedBy(PieceColor.BLACK)
}

// ─── Chess Game ──────────────────────────────────────────────────────────────

class ChessGame {

    private var _state: GameState = GameState()
    val state: GameState get() = _state

    // Stack for undo
    private val history: ArrayDeque<GameState> = ArrayDeque()

    fun reset() {
        history.clear()
        _state = GameState()
    }

    fun getLegalMoves(from: Square): List<Move> {
        if (_state.isGameOver) return emptyList()
        val piece = _state.board.pieceAt(from) ?: return emptyList()
        if (piece.color != _state.currentTurn) return emptyList()
        return _state.board.legalMoves(from)
    }

    /**
     * Make a move. Returns true if successful.
     * For pawn promotion: pass preferredPromotion (defaults to QUEEN if null).
     */
    fun makeMove(move: Move): Boolean {
        if (_state.isGameOver) return false
        val piece = _state.board.pieceAt(move.from) ?: return false
        if (piece.color != _state.currentTurn) return false

        val legalMoves = _state.board.legalMoves(move.from)
        val legalMove = legalMoves.firstOrNull {
            it.from == move.from && it.to == move.to &&
                    (it.promotionType == move.promotionType || (move.promotionType == null && it.promotionType == PieceType.QUEEN))
        } ?: return false

        // Save current state for undo
        history.addLast(_state)

        val newBoard = _state.board.applyMove(legalMove)
        val nextTurn = _state.currentTurn.opponent()

        // Update half-move clock
        val newHalfMove = if (legalMove.captured != null || piece.type == PieceType.PAWN) 0
        else _state.halfMoveClock + 1

        val newFullMove = if (_state.currentTurn == PieceColor.BLACK) _state.fullMoveNumber + 1
        else _state.fullMoveNumber

        // Determine new status
        val allMoves = newBoard.allLegalMoves(nextTurn)
        val inCheck = newBoard.isInCheck(nextTurn)
        val newStatus: GameStatus = when {
            allMoves.isEmpty() && inCheck -> GameStatus.Checkmate(winner = _state.currentTurn)
            allMoves.isEmpty() -> GameStatus.Stalemate
            newHalfMove >= 100 -> GameStatus.Draw
            inCheck -> GameStatus.Check(nextTurn)
            else -> GameStatus.Ongoing
        }

        _state = GameState(
            board = newBoard,
            currentTurn = nextTurn,
            status = newStatus,
            moveHistory = _state.moveHistory + legalMove,
            halfMoveClock = newHalfMove,
            fullMoveNumber = newFullMove
        )
        return true
    }

    fun canUndo(): Boolean = history.isNotEmpty()

    fun undo(): Boolean {
        if (history.isEmpty()) return false
        _state = history.removeLast()
        return true
    }

    /**
     * Check if a pawn move requires promotion selection.
     * Returns true if the move is a pawn reaching the last rank.
     */
    fun isPromotionMove(from: Square, to: Square): Boolean {
        val piece = _state.board.pieceAt(from) ?: return false
        if (piece.type != PieceType.PAWN) return false
        val promoRow = if (piece.color == PieceColor.WHITE) 0 else 7
        return to.row == promoRow
    }
}
