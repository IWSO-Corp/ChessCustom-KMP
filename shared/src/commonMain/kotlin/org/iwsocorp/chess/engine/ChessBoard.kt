package org.iwsocorp.chess.engine

// ─── Board ───────────────────────────────────────────────────────────────────

class ChessBoard private constructor(
    private val grid: Array<Array<Piece?>>,
    val enPassantTarget: Square? = null,
    val castlingRights: CastlingRights = CastlingRights()
) {
    companion object {
        fun initial(): ChessBoard {
            val grid = Array(8) { arrayOfNulls<Piece>(8) }
            val order = listOf(
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP,
                PieceType.QUEEN, PieceType.KING, PieceType.BISHOP,
                PieceType.KNIGHT, PieceType.ROOK
            )
            for (col in 0..7) {
                grid[0][col] = Piece(order[col], PieceColor.BLACK)
                grid[1][col] = Piece(PieceType.PAWN, PieceColor.BLACK)
                grid[6][col] = Piece(PieceType.PAWN, PieceColor.WHITE)
                grid[7][col] = Piece(order[col], PieceColor.WHITE)
            }
            return ChessBoard(grid)
        }

        fun fromGrid(
            grid: Array<Array<Piece?>>,
            enPassantTarget: Square? = null,
            castlingRights: CastlingRights = CastlingRights()
        ): ChessBoard =
            ChessBoard(Array(grid.size) { grid[it].copyOf() }, enPassantTarget, castlingRights)
    }

    fun pieceAt(sq: Square): Piece? = if (sq.isValid) grid[sq.row][sq.col] else null
    fun pieceAt(row: Int, col: Int): Piece? = grid[row][col]

    fun copy(
        grid: Array<Array<Piece?>> = this.grid,
        enPassantTarget: Square? = this.enPassantTarget,
        castlingRights: CastlingRights = this.castlingRights
    ): ChessBoard =
        ChessBoard(Array(grid.size) { grid[it].copyOf() }, enPassantTarget, castlingRights)

    // Apply a move and return a new board
    fun applyMove(move: Move): ChessBoard {
        val newGrid = Array(grid.size) { grid[it].copyOf() }
        var newEnPassant: Square? = null
        var newCastling = castlingRights.copy()

        val movingPiece = move.promotionType
            ?.let { Piece(it, move.piece.color) }
            ?: move.piece

        newGrid[move.from.row][move.from.col] = null
        newGrid[move.to.row][move.to.col] = movingPiece

        // En passant capture
        if (move.isEnPassant) {
            val captured_pawn_row =
                if (move.piece.color == PieceColor.WHITE) move.to.row + 1 else move.to.row - 1
            newGrid[captured_pawn_row][move.to.col] = null
        }

        // Double pawn push — set en passant target
        if (move.piece.type == PieceType.PAWN && kotlin.math.abs(move.to.row - move.from.row) == 2) {
            newEnPassant = Square((move.from.row + move.to.row) / 2, move.from.col)
        }

        // Castling — move rook
        if (move.isCastle) {
            val isKingSide = move.to.col > move.from.col
            val rookFromCol = if (isKingSide) 7 else 0
            val rookToCol = if (isKingSide) 5 else 3
            val rookRow = move.from.row
            newGrid[rookRow][rookToCol] = newGrid[rookRow][rookFromCol]
            newGrid[rookRow][rookFromCol] = null
        }

        // Update castling rights
        when {
            move.piece == Piece(PieceType.KING, PieceColor.WHITE) -> newCastling =
                newCastling.copy(whiteKingSide = false, whiteQueenSide = false)

            move.piece == Piece(PieceType.KING, PieceColor.BLACK) -> newCastling =
                newCastling.copy(blackKingSide = false, blackQueenSide = false)

            move.from == Square(7, 0) -> newCastling = newCastling.copy(whiteQueenSide = false)
            move.from == Square(7, 7) -> newCastling = newCastling.copy(whiteKingSide = false)
            move.from == Square(0, 0) -> newCastling = newCastling.copy(blackQueenSide = false)
            move.from == Square(0, 7) -> newCastling = newCastling.copy(blackKingSide = false)
        }

        return ChessBoard(newGrid, newEnPassant, newCastling)
    }

    // Find king position
    fun findKing(color: PieceColor): Square? {
        for (row in 0..7) for (col in 0..7) {
            val p = grid[row][col]
            if (p?.type == PieceType.KING && p.color == color) return Square(row, col)
        }
        return null
    }

    // All squares attacked by a color (ignoring legality checks)
    fun attackedSquares(byColor: PieceColor): Set<Square> {
        val attacked = mutableSetOf<Square>()
        for (row in 0..7) for (col in 0..7) {
            val piece = grid[row][col] ?: continue
            if (piece.color != byColor) continue
            val moves = pseudoLegalMoves(Square(row, col), piece, skipCastling = true)
            for (m in moves) {
                attacked.add(m.to)
            }
        }
        return attacked
    }

    fun isInCheck(color: PieceColor): Boolean {
        val kingSq = findKing(color) ?: return false
        return attackedSquares(color.opponent()).contains(kingSq)
    }

    // Pseudo-legal moves (no check validation)
    fun pseudoLegalMoves(from: Square, piece: Piece, skipCastling: Boolean = false): List<Move> {
        val moves = mutableListOf<Move>()

        fun addIfValid(to: Square, isCapture: Boolean = false) {
            if (!to.isValid) return
            val target = pieceAt(to)
            if (target?.color == piece.color) return
            val captured = target
            if (piece.type == PieceType.PAWN) {
                if (isCapture && captured == null && to != enPassantTarget) return
                if (!isCapture && captured != null) return
            }
            val isEP = piece.type == PieceType.PAWN && to == enPassantTarget && isCapture
            val promoRow = if (piece.color == PieceColor.WHITE) 0 else 7
            if (piece.type == PieceType.PAWN && to.row == promoRow) {
                for (pt in listOf(
                    PieceType.QUEEN,
                    PieceType.ROOK,
                    PieceType.BISHOP,
                    PieceType.KNIGHT
                )) {
                    moves += Move(from, to, piece, captured, promotionType = pt, isEnPassant = isEP)
                }
            } else {
                moves += Move(from, to, piece, captured, isEnPassant = isEP)
            }
        }

        fun slide(directions: List<Pair<Int, Int>>) {
            for ((dr, dc) in directions) {
                var sq = Square(from.row + dr, from.col + dc)
                while (sq.isValid) {
                    val target = pieceAt(sq)
                    if (target?.color == piece.color) break
                    moves += Move(from, sq, piece, target)
                    if (target != null) break
                    sq = Square(sq.row + dr, sq.col + dc)
                }
            }
        }

        when (piece.type) {
            PieceType.PAWN -> {
                val dir = if (piece.color == PieceColor.WHITE) -1 else 1
                val startRow = if (piece.color == PieceColor.WHITE) 6 else 1
                // Forward
                val one = Square(from.row + dir, from.col)
                if (one.isValid && pieceAt(one) == null) {
                    addIfValid(one)
                    if (from.row == startRow) {
                        val two = Square(from.row + 2 * dir, from.col)
                        if (pieceAt(two) == null) addIfValid(two)
                    }
                }
                // Diagonal captures
                for (dc in listOf(-1, 1)) addIfValid(
                    Square(from.row + dir, from.col + dc),
                    isCapture = true
                )
            }

            PieceType.KNIGHT -> {
                for ((dr, dc) in listOf(
                    -2 to -1,
                    -2 to 1,
                    -1 to -2,
                    -1 to 2,
                    1 to -2,
                    1 to 2,
                    2 to -1,
                    2 to 1
                )) {
                    addIfValid(Square(from.row + dr, from.col + dc))
                }
            }

            PieceType.BISHOP -> slide(listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1))
            PieceType.ROOK -> slide(listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1))
            PieceType.QUEEN -> slide(
                listOf(
                    -1 to -1,
                    -1 to 1,
                    1 to -1,
                    1 to 1,
                    -1 to 0,
                    1 to 0,
                    0 to -1,
                    0 to 1
                )
            )

            PieceType.KING -> {
                for ((dr, dc) in listOf(
                    -1 to -1,
                    -1 to 0,
                    -1 to 1,
                    0 to -1,
                    0 to 1,
                    1 to -1,
                    1 to 0,
                    1 to 1
                )) {
                    addIfValid(Square(from.row + dr, from.col + dc))
                }
                // Castling
                if (!skipCastling && !isInCheck(piece.color)) {
                    val row = from.row
                    val oppAttacks = attackedSquares(piece.color.opponent())
                    // King side
                    val canKS =
                        if (piece.color == PieceColor.WHITE) castlingRights.whiteKingSide else castlingRights.blackKingSide
                    if (canKS && pieceAt(Square(row, 5)) == null && pieceAt(Square(row, 6)) == null
                        && Square(row, 5) !in oppAttacks && Square(row, 6) !in oppAttacks
                    ) {
                        moves += Move(from, Square(row, 6), piece, isCastle = true)
                    }
                    // Queen side
                    val canQS =
                        if (piece.color == PieceColor.WHITE) castlingRights.whiteQueenSide else castlingRights.blackQueenSide
                    if (canQS && pieceAt(Square(row, 3)) == null && pieceAt(
                            Square(
                                row,
                                2
                            )
                        ) == null && pieceAt(Square(row, 1)) == null
                        && Square(row, 3) !in oppAttacks && Square(row, 2) !in oppAttacks
                    ) {
                        moves += Move(from, Square(row, 2), piece, isCastle = true)
                    }
                }
            }
        }
        return moves
    }

    // Legal moves for a piece (filters moves that leave king in check)
    fun legalMoves(from: Square): List<Move> {
        val piece = pieceAt(from) ?: return emptyList()
        return pseudoLegalMoves(from, piece).filter { move ->
            val newBoard = applyMove(move)
            !newBoard.isInCheck(piece.color)
        }
    }

    // All legal moves for a color
    fun allLegalMoves(color: PieceColor): List<Move> {
        val result = mutableListOf<Move>()
        for (row in 0..7) for (col in 0..7) {
            val sq = Square(row, col)
            if (pieceAt(sq)?.color == color) result += legalMoves(sq)
        }
        return result
    }

    // Captured pieces by counting missing from initial
    fun capturedBy(capturer: PieceColor): List<Piece> {
        val opponent = capturer.opponent()
        val initial = mapOf(
            PieceType.QUEEN to 1, PieceType.ROOK to 2, PieceType.BISHOP to 2,
            PieceType.KNIGHT to 2, PieceType.PAWN to 8
        )
        val onBoard = mutableMapOf<PieceType, Int>()
        for (row in 0..7) for (col in 0..7) {
            val p = grid[row][col] ?: continue
            if (p.color == opponent) onBoard[p.type] = (onBoard[p.type] ?: 0) + 1
        }
        val captured = mutableListOf<Piece>()
        for ((type, count) in initial) {
            val missing = count - (onBoard[type] ?: 0)
            repeat(missing) { captured += Piece(type, opponent) }
        }
        return captured
    }
}

// ─── Castling Rights ─────────────────────────────────────────────────────────

data class CastlingRights(
    val whiteKingSide: Boolean = true,
    val whiteQueenSide: Boolean = true,
    val blackKingSide: Boolean = true,
    val blackQueenSide: Boolean = true
)
