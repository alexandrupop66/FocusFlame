package com.alexandrupop.focusflame

import kotlin.math.roundToInt

const val FREE_DURATION_MINUTES = 25
const val PRO_ENTITLEMENT = "focusflame_pro"

data class WaxDrip(
    val progress: Float,
    val lengthFactor: Float,
    val awayMs: Long
)

data class RunningSession(
    val startedAtElapsedMs: Long,
    val startedAtWallMs: Long,
    val durationMs: Long,
    val drips: List<WaxDrip> = emptyList(),
    val distractionCount: Int = 0,
    val totalAwayMs: Long = 0L
)

data class CompletedSession(
    val id: String,
    val startedAtWallMs: Long,
    val completedAtWallMs: Long,
    val durationMs: Long,
    val distractionCount: Int,
    val totalAwayMs: Long,
    val drips: List<WaxDrip>
)

data class SessionSummary(
    val focusDurationMs: Long,
    val plannedDurationMs: Long,
    val distractionCount: Int,
    val totalAwayMs: Long,
    val drips: List<WaxDrip>
)

data class BillingState(
    val configured: Boolean = false,
    val pro: Boolean = false,
    val loading: Boolean = false,
    val packageReady: Boolean = false,
    val status: String = "RevenueCat key not set"
)

data class FocusUiState(
    val nowElapsedMs: Long = 0L,
    val selectedMinutes: Int = FREE_DURATION_MINUTES,
    val runningSession: RunningSession? = null,
    val finalSummary: SessionSummary? = null,
    val completedSessions: List<CompletedSession> = emptyList(),
    val billing: BillingState = BillingState(),
    val message: String? = null
) {
    val isPro: Boolean
        get() = billing.pro

    val lastCompleted: CompletedSession?
        get() = completedSessions.firstOrNull()

    val visibleShelf: List<CompletedSession>
        get() = if (isPro) completedSessions else completedSessions.take(1)
}

fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

fun formatMinutes(ms: Long): String {
    val minutes = (ms / 60000f).roundToInt()
    return "$minutes min"
}
