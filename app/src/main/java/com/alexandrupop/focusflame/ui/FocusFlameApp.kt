package com.alexandrupop.focusflame.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alexandrupop.focusflame.CompletedSession
import com.alexandrupop.focusflame.FREE_DURATION_MINUTES
import com.alexandrupop.focusflame.FocusUiState
import com.alexandrupop.focusflame.FocusViewModel
import com.alexandrupop.focusflame.RunningSession
import com.alexandrupop.focusflame.SessionSummary
import com.alexandrupop.focusflame.formatDuration
import com.alexandrupop.focusflame.formatMinutes
import java.text.DateFormat
import java.util.Date

@Composable
fun FocusFlameApp(
    viewModel: FocusViewModel,
    activity: Activity
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        val message = state.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            val finalSummary = state.finalSummary
            if (finalSummary != null) {
                item {
                    FinalSummaryScreen(
                        summary = finalSummary,
                        onDone = viewModel::finishSummary
                    )
                }
            } else {
                item {
                    Header(state)
                }
                item {
                    TimerPanel(
                        state = state,
                        onStart = viewModel::startSession,
                        onCancel = viewModel::cancelSession,
                        onDuration = viewModel::selectDuration
                    )
                }
                item {
                    ResultPanel(state.lastCompleted)
                }
                item {
                    ProPanel(
                        state = state,
                        onPurchase = { viewModel.purchasePro(activity) },
                        onRestore = viewModel::restorePro
                    )
                }
                if (state.visibleShelf.isNotEmpty()) {
                    items(state.visibleShelf, key = { it.id }) { session ->
                        ShelfItem(session = session, pro = state.isPro)
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun FinalSummaryScreen(
    summary: SessionSummary,
    onDone: () -> Unit
) {
    val progress = (summary.focusDurationMs.toFloat() / summary.plannedDurationMs).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Session summary",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CandleCanvas(
                    progress = progress,
                    drips = summary.drips,
                    finished = true
                )
                StatRow("Focus duration", formatDuration(summary.focusDurationMs))
                StatRow("Distractions", summary.distractionCount.toString())
                StatRow("Total time away", formatDuration(summary.totalAwayMs))
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun Header(state: FocusUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "FocusFlame",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (state.isPro) "Candle Shelf Pro" else "Free flame",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (state.isPro) "Pro" else "25", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TimerPanel(
    state: FocusUiState,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onDuration: (Int) -> Unit
) {
    val session = state.runningSession
    val progress = session?.let {
        ((state.nowElapsedMs - it.startedAtElapsedMs).toFloat() / it.durationMs).coerceIn(0f, 1f)
    } ?: state.lastCompleted?.let { 1f } ?: 0f
    val drips = session?.drips ?: state.lastCompleted?.drips.orEmpty()
    val remaining = session?.let {
        (it.durationMs - (state.nowElapsedMs - it.startedAtElapsedMs)).coerceAtLeast(0L)
    } ?: (state.selectedMinutes * 60_000L)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CandleCanvas(
                progress = progress,
                drips = drips,
                finished = session == null && state.lastCompleted != null
            )
            Text(
                text = formatDuration(remaining),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = session?.sessionLine(state.nowElapsedMs) ?: "${state.selectedMinutes} minute session",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            DurationPicker(
                selected = state.selectedMinutes,
                pro = state.isPro,
                enabled = session == null,
                onDuration = onDuration
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (session == null) {
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Light candle")
                }
            } else {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("End early")
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DurationPicker(
    selected: Int,
    pro: Boolean,
    enabled: Boolean,
    onDuration: (Int) -> Unit
) {
    val options = listOf(15, 25, 45, 60)
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { minutes ->
            FilterChip(
                selected = selected == minutes,
                onClick = { onDuration(minutes) },
                enabled = enabled && (pro || minutes == FREE_DURATION_MINUTES),
                label = { Text(if (!pro && minutes != FREE_DURATION_MINUTES) "$minutes Pro" else "$minutes") }
            )
        }
    }
}

@Composable
private fun ResultPanel(lastCompleted: CompletedSession?) {
    if (lastCompleted == null) return
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Last candle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            StatRow("Focus duration", formatMinutes(lastCompleted.durationMs))
            StatRow("Distractions", lastCompleted.distractionCount.toString())
            StatRow("Total time away", formatDuration(lastCompleted.totalAwayMs))
        }
    }
}

@Composable
private fun ProPanel(
    state: FocusUiState,
    onPurchase: () -> Unit,
    onRestore: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Candle Shelf", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (state.isPro) "Completed candles and custom durations are unlocked."
                        else "Pro unlocks the shelf and custom durations.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text(if (state.isPro) "Unlocked" else "Pro") }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.billing.status,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            if (!state.isPro) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ElevatedButton(
                        onClick = onPurchase,
                        enabled = state.billing.configured && state.billing.packageReady && !state.billing.loading
                    ) {
                        Text("Upgrade")
                    }
                    OutlinedButton(
                        onClick = onRestore,
                        enabled = state.billing.configured && !state.billing.loading
                    ) {
                        Text("Restore")
                    }
                }
            }
        }
    }
}

@Composable
private fun ShelfItem(session: CompletedSession, pro: Boolean) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(58.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (pro) Color(0xFFF3C99B) else Color(0xFFD7C2B2))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(session.completedAtWallMs)))
                Text(
                    text = "${formatMinutes(session.durationMs)} focus - ${session.distractionCount} distractions - ${formatDuration(session.totalAwayMs)} away",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!pro) {
                Text("Last", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun RunningSession.sessionLine(nowElapsedMs: Long): String {
    return "${formatDuration(nowElapsedMs - startedAtElapsedMs)} focused - $distractionCount distractions - ${formatDuration(totalAwayMs)} away"
}
