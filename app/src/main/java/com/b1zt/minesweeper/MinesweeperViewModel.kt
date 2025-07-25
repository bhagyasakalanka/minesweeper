package com.b1zt.minesweeper

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Define difficulty levels
enum class GameDifficulty(val rows: Int, val cols: Int, val mines: Int) {
    BEGINNER(9, 9, 10),
    INTERMEDIATE(16, 16, 40),
    EXPERT(16, 30, 99)
}

class MinesweeperViewModel(private val context: Context) : ViewModel() {
    // High score manager
    private val highScoreManager = HighScoreManager(context)

    // Timer state
    var elapsedTime by mutableStateOf(0L)
        private set
    private var timerJob: Job? = null
    private val timerScope = CoroutineScope(Dispatchers.Main)

    // Current difficulty level
    var difficulty by mutableStateOf(GameDifficulty.BEGINNER)
        private set

    // Remaining mines to flag
    var remainingMines by mutableStateOf(difficulty.mines)
        private set

    var game by mutableStateOf(MinesweeperGame(difficulty.rows, difficulty.cols, difficulty.mines))
        private set

    val board get() = game.board
    val isGameOver get() = game.isGameOver
    val isWin get() = game.isWin

    // Track if first move has been made, to ensure consistent mine layout
    private var hasFirstMoveMade = false
    private var minePositions = mutableListOf<Pair<Int, Int>>()

    fun reveal(row: Int, col: Int) {
        // Start timer on first move
        if (!hasFirstMoveMade) {
            startTimer()
        }

        // If this is the first move, record mine positions after they're placed
        val isFirstMove = !hasFirstMoveMade

        // Create a copy of the current game to remember the original state
        val originalState = MinesweeperGame(game.rows, game.cols, game.mines)
        for (r in 0 until game.rows) for (c in 0 until game.cols) {
            originalState.board[r][c].isMine = game.board[r][c].isMine
            originalState.board[r][c].state = game.board[r][c].state
            originalState.board[r][c].adjacentMines = game.board[r][c].adjacentMines
        }

        // Perform the reveal operation on the current game
        game.reveal(row, col)

        // After first move, store mine positions for consistency
        if (isFirstMove) {
            hasFirstMoveMade = true
            minePositions.clear()
            for (r in 0 until game.rows) {
                for (c in 0 until game.cols) {
                    if (game.board[r][c].isMine) {
                        minePositions.add(Pair(r, c))
                    }
                }
            }
        }

        // Debug logging to understand state changes
        val changedCells = mutableListOf<String>()
        for (r in 0 until game.rows) for (c in 0 until game.cols) {
            if (originalState.board[r][c].state != game.board[r][c].state) {
                changedCells.add("($r,$c)")
            }
        }
        println("Changed cells after reveal: $changedCells")

        // Create a completely new game instance with fresh state
        val updatedGame = MinesweeperGame(game.rows, game.cols, game.mines)

        // Set the firstMove state to match current game
        if (hasFirstMoveMade) {
            // Force firstMove to false in the new game by doing a dummy reveal and reset
            updatedGame.reveal(0, 0)
            for (r in 0 until updatedGame.rows) for (c in 0 until updatedGame.cols) {
                updatedGame.board[r][c].state = CellState.HIDDEN
                updatedGame.board[r][c].isMine = false
                updatedGame.board[r][c].adjacentMines = 0
            }
            updatedGame.isGameOver = false
            updatedGame.isWin = false

            // Place mines in exactly the same positions
            for ((r, c) in minePositions) {
                updatedGame.board[r][c].isMine = true
            }

            // Recalculate adjacent mines
            for (r in 0 until updatedGame.rows) for (c in 0 until updatedGame.cols) {
                updatedGame.board[r][c].adjacentMines = countAdjacentMines(updatedGame, r, c)
            }
        }

        // Copy all cell states from the current game, which includes all cascaded reveals
        for (r in 0 until game.rows) for (c in 0 until game.cols) {
            updatedGame.board[r][c].state = game.board[r][c].state
        }
        updatedGame.isGameOver = game.isGameOver
        updatedGame.isWin = game.isWin

        // Update the game reference to trigger UI recomposition
        game = updatedGame

        // If game is over, stop timer and save score if won
        if (game.isGameOver) {
            stopTimer()
            if (game.isWin) {
                highScoreManager.saveScore(difficulty, elapsedTime)
            }
        }
    }

    fun toggleFlag(row: Int, col: Int) {
        // Check if cell is already flagged to update mine counter
        val isFlagged = game.board[row][col].state == CellState.FLAGGED

        // Perform the flag operation
        game.toggleFlag(row, col)

        // Update remaining mine count
        if (isFlagged) {
            remainingMines++
        } else if (game.board[row][col].state == CellState.FLAGGED) {
            remainingMines--
        }

        // Create a new game instance
        val updatedGame = MinesweeperGame(game.rows, game.cols, game.mines)

        // Ensure mine layout is consistent
        if (hasFirstMoveMade) {
            // Clear any default mines
            for (r in 0 until updatedGame.rows) {
                for (c in 0 until updatedGame.cols) {
                    updatedGame.board[r][c].isMine = false
                }
            }

            // Set mines based on stored positions
            for ((r, c) in minePositions) {
                updatedGame.board[r][c].isMine = true
            }

            // Recalculate adjacent mines
            for (r in 0 until updatedGame.rows) {
                for (c in 0 until updatedGame.cols) {
                    updatedGame.board[r][c].adjacentMines = countAdjacentMines(updatedGame, r, c)
                }
            }
        }

        // Copy the game state
        for (r in 0 until game.rows) for (c in 0 until game.cols) {
            updatedGame.board[r][c].state = game.board[r][c].state
        }
        updatedGame.isGameOver = game.isGameOver
        updatedGame.isWin = game.isWin

        // Update the game reference
        game = updatedGame

        // If game is over, stop timer and save score if won
        if (game.isGameOver) {
            stopTimer()
            if (game.isWin) {
                highScoreManager.saveScore(difficulty, elapsedTime)
            }
        }
    }

    fun reset() {
        game = MinesweeperGame(difficulty.rows, difficulty.cols, difficulty.mines)
        hasFirstMoveMade = false
        minePositions.clear()
        remainingMines = difficulty.mines
        resetTimer()
    }

    fun changeDifficulty(newDifficulty: GameDifficulty) {
        difficulty = newDifficulty
        game = MinesweeperGame(difficulty.rows, difficulty.cols, difficulty.mines)
        hasFirstMoveMade = false
        minePositions.clear()
        remainingMines = difficulty.mines
        resetTimer()
    }

    // Get high scores for a specific difficulty level
    fun getHighScores(difficulty: GameDifficulty): List<HighScore> {
        return highScoreManager.getScores(difficulty)
    }

    // Timer functions
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = timerScope.launch {
            while (true) {
                delay(1000)
                elapsedTime++
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    private fun resetTimer() {
        stopTimer()
        elapsedTime = 0
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        timerScope.cancel()
    }

    // Helper function to count adjacent mines
    private fun countAdjacentMines(game: MinesweeperGame, row: Int, col: Int): Int {
        var count = 0
        for (dr in -1..1) for (dc in -1..1) {
            val nr = row + dr
            val nc = col + dc
            if (nr in 0 until game.rows && nc in 0 until game.cols && game.board[nr][nc].isMine) {
                count++
            }
        }
        return count
    }
}
