package com.b1zt.minesweeper

enum class CellState { HIDDEN, REVEALED, FLAGGED }

data class Cell(
    val row: Int,
    val col: Int,
    var isMine: Boolean = false,
    var state: CellState = CellState.HIDDEN,
    var adjacentMines: Int = 0
)

class MinesweeperGame(val rows: Int, val cols: Int, val mines: Int) {
    val board: Array<Array<Cell>> = Array(rows) { r -> Array(cols) { c -> Cell(r, c) } }
    var isGameOver = false
    var isWin = false
    private var firstMove = true

    fun reset() {
        for (row in board) for (cell in row) {
            cell.isMine = false
            cell.state = CellState.HIDDEN
            cell.adjacentMines = 0
        }
        isGameOver = false
        isWin = false
        firstMove = true
    }

    private fun placeMines(excludeRow: Int, excludeCol: Int) {
        var placed = 0
        val positions = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until rows) for (c in 0 until cols) {
            if (r != excludeRow || c != excludeCol) positions.add(r to c)
        }
        positions.shuffle()
        for ((r, c) in positions.take(mines)) {
            board[r][c].isMine = true
            placed++
        }
        for (r in 0 until rows) for (c in 0 until cols) {
            board[r][c].adjacentMines = countAdjacentMines(r, c)
        }
    }

    private fun countAdjacentMines(row: Int, col: Int): Int {
        var count = 0
        for (dr in -1..1) for (dc in -1..1) {
            val nr = row + dr
            val nc = col + dc
            if (nr in 0 until rows && nc in 0 until cols && board[nr][nc].isMine) count++
        }
        return count
    }

    fun reveal(row: Int, col: Int) {
        if (isGameOver) return
        val cell = board[row][col]
        if (cell.state != CellState.HIDDEN) return
        if (firstMove) {
            placeMines(row, col)
            firstMove = false
        }
        cell.state = CellState.REVEALED
        if (cell.isMine) {
            isGameOver = true
            revealAllMines()
            return
        }
        if (cell.adjacentMines == 0) {
            for (dr in -1..1) for (dc in -1..1) {
                val nr = row + dr
                val nc = col + dc
                if (nr in 0 until rows && nc in 0 until cols) {
                    if (board[nr][nc].state == CellState.HIDDEN) reveal(nr, nc)
                }
            }
        }
        checkWin()
    }

    fun toggleFlag(row: Int, col: Int) {
        val cell = board[row][col]
        if (cell.state == CellState.HIDDEN) cell.state = CellState.FLAGGED
        else if (cell.state == CellState.FLAGGED) cell.state = CellState.HIDDEN

        // Check for win condition after flagging
        checkWinAfterFlagging()
    }

    private fun checkWinAfterFlagging() {
        if (isGameOver) return

        // Count all cells that are either revealed or flagged
        var allMinesFlagged = true
        var allSafeCellsRevealed = true

        for (row in board) for (cell in row) {
            if (cell.isMine && cell.state != CellState.FLAGGED) {
                allMinesFlagged = false
            }
            if (!cell.isMine && cell.state != CellState.REVEALED) {
                allSafeCellsRevealed = false
            }
        }

        // Win if all mines are flagged AND all safe cells are revealed
        if (allMinesFlagged && allSafeCellsRevealed) {
            isWin = true
            isGameOver = true
        }
    }

    private fun revealAllMines() {
        for (row in board) for (cell in row) {
            if (cell.isMine) cell.state = CellState.REVEALED
        }
    }

    private fun checkWin() {
        if (isGameOver) return
        for (row in board) for (cell in row) {
            if (!cell.isMine && cell.state != CellState.REVEALED) return
        }
        isWin = true
        isGameOver = true
    }
}
