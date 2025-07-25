package com.b1zt.minesweeper

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MinesweeperBoard(
    viewModel: MinesweeperViewModel = viewModel()
) {
    val board = viewModel.board
    val isGameOver = viewModel.isGameOver
    val isWin = viewModel.isWin
    val remainingMines = viewModel.remainingMines
    val difficulty = viewModel.difficulty
    val elapsedTime = viewModel.elapsedTime

    var showDifficultyMenu by remember { mutableStateOf(false) }
    var showHighScores by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Game header with status face, timer and mine counter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Mine counter
            Surface(
                color = Color(0xFF000000),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = remainingMines.toString(),
                    color = Color.Red,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Happy/Sad Face Reset Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFFDDDDDD),
                        shape = MaterialTheme.shapes.small
                    )
                    .border(
                        width = 2.dp,
                        color = Color.Gray,
                        shape = MaterialTheme.shapes.small
                    )
                    .clickable { viewModel.reset() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        isWin -> "😎" // Cool face for win
                        isGameOver -> "😵" // Dead face for loss
                        else -> "🙂" // Smiling face for normal play
                    },
                    fontSize = 26.sp
                )
            }

            // Timer display
            Surface(
                color = Color(0xFF000000),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = formatTime(elapsedTime),
                    color = Color.Red,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // Controls row with difficulty selector and high scores button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Difficulty selector
            Box {
                Button(onClick = { showDifficultyMenu = true }) {
                    Text("Difficulty: ${difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}")
                }

                DropdownMenu(
                    expanded = showDifficultyMenu,
                    onDismissRequest = { showDifficultyMenu = false }
                ) {
                    GameDifficulty.values().forEach { diffLevel ->
                        DropdownMenuItem(
                            text = { Text(diffLevel.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                viewModel.changeDifficulty(diffLevel)
                                showDifficultyMenu = false
                            }
                        )
                    }
                }
            }

            // High Scores button
            Button(onClick = { showHighScores = true }) {
                Text("High Scores")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Game board - now with horizontal scroll for wide boards
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (r in board.indices) {
                    Row {
                        for (c in board[r].indices) {
                            val cell = board[r][c]
                            CellDisplay(
                                cell = cell,
                                onReveal = {
                                    if (!isGameOver) viewModel.reveal(r, c)
                            },
                                onFlag = {
                                    if (!isGameOver) viewModel.toggleFlag(r, c)
                            }
                            )
                        }
                    }
                }
            }
        }

        // Instructions
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Left click: Reveal cell",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(vertical = 2.dp)
        )
        Text(
            "Long press: Flag mine",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        // High Scores Dialog
        if (showHighScores) {
            HighScoresDialog(
                viewModel = viewModel,
                onDismiss = { showHighScores = false }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CellDisplay(
    cell: Cell,
    onReveal: () -> Unit,
    onFlag: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .border(1.dp, Color.Gray)
            .background(
                when (cell.state) {
                    CellState.REVEALED -> Color.LightGray
                    CellState.FLAGGED -> Color(0xFFFFF59D)
                    else -> Color(0xFFB0BEC5)
                }
            )
            .combinedClickable(
                onClick = onReveal,
                onLongClick = onFlag
            ),
        contentAlignment = Alignment.Center
    ) {
        when (cell.state) {
            CellState.REVEALED -> {
                if (cell.isMine) {
                    Text("💣", fontSize = 18.sp)
                } else if (cell.adjacentMines > 0) {
                    // Classic Minesweeper number colors for better visibility
                    val numberColor = when(cell.adjacentMines) {
                        1 -> Color(0xFF0000FF) // Blue
                        2 -> Color(0xFF007B00) // Green
                        3 -> Color(0xFFFF0000) // Red
                        4 -> Color(0xFF00007B) // Dark Blue
                        5 -> Color(0xFF7B0000) // Dark Red
                        6 -> Color(0xFF007B7B) // Teal
                        7 -> Color(0xFF000000) // Black
                        else -> Color(0xFF7B7B7B) // Dark Gray
                    }
                    Text(
                        text = cell.adjacentMines.toString(),
                        fontSize = 16.sp,
                        color = numberColor,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
            CellState.FLAGGED -> Text("🚩", fontSize = 16.sp)
            else -> {}
        }
    }
}

@Composable
fun HighScoresDialog(
    viewModel: MinesweeperViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("High Scores") },
        text = {
            Column {
                var selectedDifficulty by remember { mutableStateOf(GameDifficulty.BEGINNER) }

                // Difficulty tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GameDifficulty.values().forEach { difficulty ->
                        Button(
                            onClick = { selectedDifficulty = difficulty },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedDifficulty == difficulty)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                difficulty.name.take(1),
                                color = if (selectedDifficulty == difficulty)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // High scores list for selected difficulty
                val highScores = viewModel.getHighScores(selectedDifficulty)
                if (highScores.isEmpty()) {
                    Text("No scores yet for ${selectedDifficulty.name.lowercase().replaceFirstChar { it.uppercase() }}")
                } else {
                    Column {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rank", style = MaterialTheme.typography.titleSmall)
                            Text("Time", style = MaterialTheme.typography.titleSmall)
                            Text("Date", style = MaterialTheme.typography.titleSmall)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Scores
                        highScores.forEachIndexed { index, score ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${index + 1}.", style = MaterialTheme.typography.bodyMedium)
                                Text(formatTime(score.time), style = MaterialTheme.typography.bodyMedium)
                                Text(score.date, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Helper function to format time in MM:SS format
fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
