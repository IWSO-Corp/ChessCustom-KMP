package org.iwsocorp.chess.engine

// ─── Color ───────────────────────────────────────────────────────────────────

enum class PieceColor {
    WHITE, BLACK;

    fun opponent(): PieceColor = if (this == WHITE) BLACK else WHITE
    fun displayName(): String = if (this == WHITE) "Putih" else "Hitam"
}

// ─── Piece Type ──────────────────────────────────────────────────────────────

enum class PieceType(val symbol: String, val unicodeWhite: String, val unicodeBlack: String) {
    KING("K", "♔", "♚"),
    QUEEN("Q", "♕", "♛"),
    ROOK("R", "♖", "♜"),
    BISHOP("B", "♗", "♝"),
    KNIGHT("N", "♘", "♞"),
    PAWN("P", "♙", "♟");
}

// ─── Piece ───────────────────────────────────────────────────────────────────

data class Piece(val type: PieceType, val color: PieceColor) {
    val unicode: String get() = if (color == PieceColor.WHITE) type.unicodeWhite else type.unicodeBlack
    override fun toString(): String = "${color.name[0]}${type.symbol}"
}

// ─── Square ──────────────────────────────────────────────────────────────────

data class Square(val row: Int, val col: Int) {
    val isValid: Boolean get() = row in 0..7 && col in 0..7
    val fileChar: Char get() = ('a' + col)
    val rankChar: Char get() = ('8' - row)
    val notation: String get() = "$fileChar$rankChar"

    operator fun plus(other: Square): Square = Square(row + other.row, col + other.col)
    operator fun times(scalar: Int): Square = Square(row * scalar, col * scalar)

    companion object {
        fun fromNotation(notation: String): Square? {
            if (notation.length != 2) return null
            val col = notation[0] - 'a'
            val row = '8' - notation[1]
            return Square(row, col).takeIf { it.isValid }
        }
    }
}

// ─── Move ────────────────────────────────────────────────────────────────────

data class Move(
    val from: Square,
    val to: Square,
    val piece: Piece,
    val captured: Piece? = null,
    val promotionType: PieceType? = null,
    val isCastle: Boolean = false,
    val isEnPassant: Boolean = false
) {
    val notation: String
        get() {
            val capture = if (captured != null || isEnPassant) "x" else ""
            val promo = if (promotionType != null) "=${promotionType.symbol}" else ""
            val pref =
                if (piece.type == PieceType.PAWN && capture.isNotEmpty()) "${piece.type.symbol.lowercase()}" else piece.type.symbol
            return "$pref${from.notation}$capture${to.notation}$promo"
        }
    val displayNotation: String
        get() {
            return if (isCastle) {
                if (to.col > from.col) "O-O" else "O-O-O"
            } else notation
        }
}

// ─── Game Status ─────────────────────────────────────────────────────────────

sealed class GameStatus {
    object Ongoing : GameStatus()
    data class Check(val color: PieceColor) : GameStatus()
    data class Checkmate(val winner: PieceColor) : GameStatus()
    object Stalemate : GameStatus()
    object Draw : GameStatus()
}
