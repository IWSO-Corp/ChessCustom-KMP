package org.iwsocorp.chess

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import org.iwsocorp.chess.ui.ChessScreen

@Composable
fun App() {
    MaterialTheme {
        Surface {
            ChessScreen()
        }
    }
}