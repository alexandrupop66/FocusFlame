package com.alexandrupop.focusflame

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("focus_flame_sessions", Context.MODE_PRIVATE)

    fun loadCompleted(): List<CompletedSession> {
        val raw = prefs.getString(KEY_COMPLETED, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    add(array.getJSONObject(index).toCompletedSession())
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveCompleted(sessions: List<CompletedSession>) {
        val array = JSONArray()
        sessions.take(MAX_SESSIONS).forEach { session ->
            array.put(session.toJson())
        }
        prefs.edit().putString(KEY_COMPLETED, array.toString()).apply()
    }

    private fun JSONObject.toCompletedSession(): CompletedSession {
        val dripArray = optJSONArray("drips") ?: JSONArray()
        val drips = buildList {
            for (index in 0 until dripArray.length()) {
                val drip = dripArray.getJSONObject(index)
                add(
                    WaxDrip(
                        progress = drip.optDouble("progress", 0.0).toFloat(),
                        lengthFactor = drip.optDouble("lengthFactor", 0.0).toFloat(),
                        awayMs = drip.optLong("awayMs", 0L)
                    )
                )
            }
        }
        return CompletedSession(
            id = getString("id"),
            startedAtWallMs = getLong("startedAtWallMs"),
            completedAtWallMs = getLong("completedAtWallMs"),
            durationMs = getLong("durationMs"),
            distractionCount = getInt("distractionCount"),
            totalAwayMs = getLong("totalAwayMs"),
            drips = drips
        )
    }

    private fun CompletedSession.toJson(): JSONObject {
        val dripArray = JSONArray()
        drips.forEach { drip ->
            dripArray.put(
                JSONObject()
                    .put("progress", drip.progress)
                    .put("lengthFactor", drip.lengthFactor)
                    .put("awayMs", drip.awayMs)
            )
        }
        return JSONObject()
            .put("id", id)
            .put("startedAtWallMs", startedAtWallMs)
            .put("completedAtWallMs", completedAtWallMs)
            .put("durationMs", durationMs)
            .put("distractionCount", distractionCount)
            .put("totalAwayMs", totalAwayMs)
            .put("drips", dripArray)
    }

    private companion object {
        const val KEY_COMPLETED = "completed"
        const val MAX_SESSIONS = 50
    }
}
