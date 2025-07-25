package com.b1zt.minesweeper

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HighScore(
    val time: Long,
    val date: String
)

class HighScoreManager(private val context: Context) {
    private val sharedPrefs = context.getSharedPreferences("minesweeper_scores", Context.MODE_PRIVATE)

    fun saveScore(difficulty: GameDifficulty, time: Long) {
        val scores = getScores(difficulty).toMutableList()
        val dateFormat = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        scores.add(HighScore(time, currentDate))
        scores.sortBy { it.time } // Sort by time (ascending)

        // Keep only top 10 scores
        val topScores = scores.take(10)

        // Save to SharedPreferences
        val jsonArray = JSONArray()
        topScores.forEach { score ->
            val jsonObject = JSONObject()
            jsonObject.put("time", score.time)
            jsonObject.put("date", score.date)
            jsonArray.put(jsonObject)
        }

        sharedPrefs.edit()
            .putString(difficulty.name, jsonArray.toString())
            .apply()
    }

    fun getScores(difficulty: GameDifficulty): List<HighScore> {
        val jsonString = sharedPrefs.getString(difficulty.name, "[]")
        val jsonArray = JSONArray(jsonString)
        val scores = mutableListOf<HighScore>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val time = jsonObject.getLong("time")
            val date = jsonObject.getString("date")
            scores.add(HighScore(time, date))
        }

        return scores
    }
}
