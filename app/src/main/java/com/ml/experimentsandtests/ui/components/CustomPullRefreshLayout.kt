package com.ml.experimentsandtests.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RefreshPage(modifier: Modifier = Modifier) {
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null // Disable overscroll otherwise it consumes the drag before we get the chance
    ) {
        val state = rememberPullState(config = PullStateConfig(heightMax = 70.dp, heightRefreshing = 50.dp))
        LaunchedEffect(state.isRefreshing) {
            if (state.isRefreshing) {
                delay(2000)
                state.finishRefresh()
            }
        }

        PullToRefreshLayout(
            pullState = state,
        ) {
            LazyColumn(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(top = state.insetTop + 16.dp)
            ) {
                items(20) {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                            .height(128.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(text = "") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PullToRefreshLayout(
    modifier: Modifier = Modifier,
    pullState: PullState = rememberPullState(),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .nestedScroll(pullState.scrollConnection),
    ) {
        Indicator(pullState = pullState)
        Column {
            // This invisible spacer height + current top inset is always equals max top inset to keep scroll speed constant
            Spacer(modifier = Modifier.height(LocalDensity.current.run { pullState.maxInsetTop.toDp() } - pullState.insetTop))

            Surface(
                modifier = Modifier.graphicsLayer {
                    translationY = pullState.offsetY
                    shape = RoundedCornerShape(
                        topStart = 36.dp * pullState.progressRefreshTrigger,
                        topEnd = 36.dp * pullState.progressRefreshTrigger,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                    clip = true
                },
                color = Color.Transparent
            ) {
                content()
            }
        }
    }
}

@Composable
fun Indicator(
    pullState: PullState
) {
    val hapticFeedback = LocalHapticFeedback.current

    val scale = remember { Animatable(1f) }

    // Pop the indicator once shortly when reaching refresh trigger offset. Also trigger some haptic feedback
    LaunchedEffect(pullState.progressRefreshTrigger >= 1f) {
        if (pullState.progressRefreshTrigger >= 1f && !pullState.isRefreshing) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            scale.snapTo(1.05f)
            scale.animateTo(1.0f, tween(100))
        }
    }

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .height(
                maxOf(
                    24.dp,
                    pullState.config.heightMax * pullState.progressHeightMax - pullState.insetTop
                )
            )
            .fillMaxWidth()
        ,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .scale(scale.value)
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pullState.isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp),
                    strokeWidth = 2.dp,
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.primaryContainer)
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp),
                    strokeWidth = 2.dp,
                    progress = { pullState.progressRefreshTrigger },
                    color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier,

                text = when {
                    pullState.isRefreshing -> "Refreshing"
                    pullState.progressRefreshTrigger >= 1f -> "Release to refresh"
                    else -> "Pull to refresh"
                },
                style = MaterialTheme.typography.labelLarge,
                color = contentColorFor(backgroundColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    }
}

@Composable
fun rememberPullState(
    config: PullStateConfig = PullStateConfig()
): PullState {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val insetTop = WindowInsets.statusBars.getTop(density)

    return remember(insetTop, config, density, scope) { PullState(insetTop, config, density, scope) }
}

data class PullStateConfig(
    val heightRefreshing: Dp = 90.dp,
    val heightMax: Dp = 150.dp,
) {
    init {
        require(heightMax >= heightRefreshing)
    }
}

class PullState internal constructor(
    val maxInsetTop: Int,
    val config: PullStateConfig,
    private val density: Density,
    private val scope: CoroutineScope,
) {
    private val heightRefreshing = with(density) { config.heightRefreshing.toPx() }
    private val heightMax = with(density) { config.heightMax.toPx() }

    private val _offsetY = Animatable(0f)
    val offsetY: Float
        get() = _offsetY.value

    // 1f -> Refresh triggered on release
    val progressRefreshTrigger: Float get() = (offsetY / heightRefreshing).coerceIn(0f, 1f)

    // 1f -> Max drag amount reached
    val progressHeightMax: Float get() = (offsetY / heightMax).coerceIn(0f, 1f)

    // Use this for your content's top padding. Only relevant when app is drawing behind status bar
    val insetTop: Dp get() = with(density) { (maxInsetTop - maxInsetTop * progressRefreshTrigger).toDp() }

    // User drag in progress
    var isDragging by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var isEnabled by mutableStateOf(true)
        private set

    suspend fun settle(offsetY: Float) {
        _offsetY.animateTo(offsetY)
    }

    fun finishRefresh() {
        isEnabled = false
        scope.launch {
            settle(0f)
            isRefreshing = false
            isEnabled = true
        }
    }

    val scrollConnection = object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            when {
                !isEnabled -> return Offset.Zero
                available.y > 0 && source == NestedScrollSource.UserInput -> {
                    // 1. User is dragging
                    // 2. Scrollable container reached the top (OR max drag reached and neither scroll container nor P2R are interested. Poor available Offset...)
                    // 3. There is still drag available that the scrollable container did not consume
                    // -> Start drag. Because next frame offsetY will be > 0f, onPreScroll will take over from here
                    isDragging = true
                    scope.launch {
                        _offsetY.snapTo((offsetY + available.y).coerceIn(0f, heightMax))
                    }
                }
            }

            return Offset.Zero
        }

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            when {
                !isEnabled -> return Offset.Zero
                offsetY > 0 && source == NestedScrollSource.Drag -> {
                    // Consumes the drag as long as the indicator is visible
                    isDragging = true
                    val newOffset = offsetY + available.y

                    // Surplus drag amount is not consumed
                    val remaining = when {
                        newOffset > heightMax -> newOffset - heightMax
                        newOffset < 0f -> newOffset
                        else -> 0f
                    }

                    scope.launch {
                        _offsetY.snapTo(newOffset.coerceIn(0f, heightMax))
                    }

                    return Offset(0f, (available.y - remaining))
                }
            }

            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (!isEnabled) return Velocity.Zero

            isDragging = false

            when {
                // When refreshing and a drag stops, either settle to 0f or heightRefreshing,
                isRefreshing -> {
                    val target = when {
                        heightRefreshing - offsetY < heightRefreshing / 2 -> heightRefreshing
                        else -> 0f
                    }

                    scope.launch {
                        settle(target)
                    }

                    // Consume the velocity as long as the indicator is visible
                    return if (offsetY == 0f) Velocity.Zero else available
                }

                // Trigger refresh
                offsetY >= heightRefreshing -> {
                    isRefreshing = true
                    scope.launch {
                        settle(heightRefreshing)
                    }
                }

                // Drag cancelled, go back to 0f
                else -> {
                    scope.launch {
                        settle(0f)
                    }
                }
            }

            return Velocity.Zero
        }
    }
}