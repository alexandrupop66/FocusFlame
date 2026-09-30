package com.alexandrupop.focusflame

import android.app.Application
import android.content.Context
import android.os.PowerManager
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.min

class FocusViewModel(
    private val app: FocusFlameApplication
) : AndroidViewModel(app), DefaultLifecycleObserver {
    private val store = app.sessionStore
    private val powerManager = app.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val internalState = MutableStateFlow(
        FocusUiState(
            nowElapsedMs = SystemClock.elapsedRealtime(),
            completedSessions = store.loadCompleted()
        )
    )

    val uiState: StateFlow<FocusUiState> = combine(
        internalState,
        app.revenueCatManager.state
    ) { state, billing ->
        state.copy(billing = billing)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = internalState.value.copy(billing = app.revenueCatManager.state.value)
    )

    private var awayStartedAtElapsedMs: Long? = null
    private var awayProgress: Float = 0f

    init {
        viewModelScope.launch {
            while (true) {
                val now = SystemClock.elapsedRealtime()
                internalState.update { it.copy(nowElapsedMs = now) }
                finishIfComplete(now)
                delay(250)
            }
        }
    }

    fun selectDuration(minutes: Int) {
        val isPro = uiState.value.isPro
        if (!isPro && minutes != FREE_DURATION_MINUTES) {
            internalState.update { it.copy(message = "Custom durations are in Candle Shelf Pro") }
            return
        }
        internalState.update { it.copy(selectedMinutes = minutes, message = null) }
    }

    fun startSession() {
        val state = uiState.value
        val minutes = if (state.isPro) state.selectedMinutes else FREE_DURATION_MINUTES
        val nowElapsed = SystemClock.elapsedRealtime()
        internalState.update {
            it.copy(
                selectedMinutes = minutes,
                runningSession = RunningSession(
                    startedAtElapsedMs = nowElapsed,
                    startedAtWallMs = System.currentTimeMillis(),
                    durationMs = minutes * 60_000L
                ),
                finalSummary = null,
                message = null
            )
        }
    }

    fun cancelSession() {
        val now = SystemClock.elapsedRealtime()
        awayStartedAtElapsedMs = null
        internalState.update { state ->
            val session = state.runningSession ?: return@update state
            state.copy(
                runningSession = null,
                finalSummary = SessionSummary(
                    focusDurationMs = (now - session.startedAtElapsedMs).coerceAtLeast(0L),
                    plannedDurationMs = session.durationMs,
                    distractionCount = session.distractionCount,
                    totalAwayMs = session.totalAwayMs,
                    drips = session.drips
                ),
                message = null
            )
        }
    }

    fun finishSummary() {
        internalState.update { it.copy(finalSummary = null, message = null) }
    }

    fun clearMessage() {
        internalState.update { it.copy(message = null) }
    }

    fun purchasePro(activity: android.app.Activity) {
        app.revenueCatManager.purchase(activity)
    }

    fun restorePro() {
        app.revenueCatManager.restore()
    }

    override fun onStop(owner: LifecycleOwner) {
        val session = internalState.value.runningSession ?: return
        if (!powerManager.isInteractive) return
        val now = SystemClock.elapsedRealtime()
        awayStartedAtElapsedMs = now
        awayProgress = ((now - session.startedAtElapsedMs).toFloat() / session.durationMs).coerceIn(0f, 1f)
    }

    override fun onStart(owner: LifecycleOwner) {
        val startedAway = awayStartedAtElapsedMs ?: return
        awayStartedAtElapsedMs = null
        val now = SystemClock.elapsedRealtime()
        val awayMs = (now - startedAway).coerceAtLeast(0L)
        if (awayMs < 1000L) {
            finishIfComplete(now)
            return
        }

        internalState.update { state ->
            val session = state.runningSession ?: return@update state
            val drip = WaxDrip(
                progress = awayProgress,
                lengthFactor = min(1f, 0.18f + awayMs / 180_000f),
                awayMs = awayMs
            )
            state.copy(
                runningSession = session.copy(
                    drips = session.drips + drip,
                    distractionCount = session.distractionCount + 1,
                    totalAwayMs = session.totalAwayMs + awayMs
                ),
                message = "Distraction marked: ${formatDuration(awayMs)} away"
            )
        }
        finishIfComplete(now)
    }

    private fun finishIfComplete(nowElapsedMs: Long) {
        val session = internalState.value.runningSession ?: return
        if (nowElapsedMs - session.startedAtElapsedMs < session.durationMs) return

        val completed = CompletedSession(
            id = UUID.randomUUID().toString(),
            startedAtWallMs = session.startedAtWallMs,
            completedAtWallMs = System.currentTimeMillis(),
            durationMs = session.durationMs,
            distractionCount = session.distractionCount,
            totalAwayMs = session.totalAwayMs,
            drips = session.drips
        )
        internalState.update { state ->
            val updated = listOf(completed) + state.completedSessions
            store.saveCompleted(updated)
            state.copy(
                runningSession = null,
                completedSessions = updated,
                message = "Candle finished"
            )
        }
    }
}
