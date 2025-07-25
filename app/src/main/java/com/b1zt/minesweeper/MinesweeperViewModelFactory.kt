package com.b1zt.minesweeper

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MinesweeperViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MinesweeperViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MinesweeperViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
