package com.ml.experimentsandtests.ui.components

import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.generateDecayAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2
import kotlin.math.roundToInt

@Composable
fun NestedScrollWithCollapsibleHeader() {
    val collapsingHeaderState = detailCollapsingHeaderState()

CollapsingHeader(
    state = collapsingHeaderState,
    headerContent = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.4f)
                .background(Color.Blue)
        )
    },
    body = {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            content = {
                items(100) {
                    Text(
                        text = "Item $it",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        )
    }
)


}

@Composable
fun detailCollapsingHeaderState(): CollapsingHeaderState {
    val density = LocalDensity.current
    val collapsedHeight = with(density) { 56.dp.toPx() } +
            WindowInsets.statusBars.getTop(density).toFloat() +
            WindowInsets.statusBars.getBottom(density).toFloat()
    return remember(collapsedHeight) {
        CollapsingHeaderState(
            initialExpandedHeight = with(density) { 400.dp.toPx() },
            initialCollapsedHeight = collapsedHeight,
            density = density
        )
    }
}

enum class CollapsingHeaderStatus {
    Collapsed, Expanded
}

@Stable
@OptIn(ExperimentalFoundationApi::class)
class CollapsingHeaderState(
    initialCollapsedHeight: Float,
    initialExpandedHeight: Float,
    density: Density
) {

    private var anchors by mutableLongStateOf(
        Anchors(
            collapsedHeight = initialCollapsedHeight,
            expandedHeight = initialExpandedHeight
        ).packedValue
    )

    var expandedHeight
        get() = Anchors(anchors).expandedHeight
        set(value) {
            anchors = Anchors(
                collapsedHeight = collapsedHeight,
                expandedHeight = value
            ).packedValue
            updateAnchors()
        }

    var collapsedHeight
        get() = Anchors(anchors).collapsedHeight
        set(value) {
            anchors = Anchors(
                collapsedHeight = value,
                expandedHeight = expandedHeight
            ).packedValue
            updateAnchors()
        }

    val translation get() = expandedHeight - anchoredDraggableState.requireOffset()

    val progress get() = translation / (expandedHeight - collapsedHeight)

    // This should not be externally visible. It is an implementation detail
    internal val anchoredDraggableState = AnchoredDraggableState(
        initialValue = CollapsingHeaderStatus.Collapsed,
        positionalThreshold = { distance: Float -> distance * 0.5f },
        velocityThreshold = { 100f },
        snapAnimationSpec = tween(),
        decayAnimationSpec =  SplineBasedFloatDecayAnimationSpec(density).generateDecayAnimationSpec(),
        anchors = currentDraggableAnchors()
    )

    private fun updateAnchors() = anchoredDraggableState.updateAnchors(
        currentDraggableAnchors()
    )

    @OptIn(ExperimentalFoundationApi::class)
    private fun currentDraggableAnchors() = DraggableAnchors {
        CollapsingHeaderStatus.Collapsed at expandedHeight
        CollapsingHeaderStatus.Expanded at collapsedHeight
    }
}

/**
 * A collapsing header implementation that has anchored positions.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CollapsingHeader(
    state: CollapsingHeaderState,
    headerContent: @Composable () -> Unit,
    body: @Composable () -> Unit,
) {
    val scrollableState = rememberScrollableState(
        consumeScrollDelta = state.anchoredDraggableState::dispatchRawDelta
    )
    Box(
        // TODO: Make this composable nestable by implementing nested scroll here as well
        modifier = Modifier.scrollable(
            state = scrollableState,
            orientation = Orientation.Vertical,
        )
    ) {
        Box(
            modifier = Modifier
                .onSizeChanged { state.expandedHeight = it.height.toFloat() },
            content = {
                headerContent()
            }
        )
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = 0,
                        y = state.anchoredDraggableState.offset.roundToInt()
                    )
                }
                .anchoredDraggable(
                    state = state.anchoredDraggableState,
                    orientation = Orientation.Vertical
                )
                .nestedScroll(
                    connection = state.anchoredDraggableState.nestedScrollConnection(),
                ),
            content = {
                body()
            }
        )
    }
}

/**
 * Packed float class to use [mutableLongStateOf] to hold state for expanded and collapsed heights.
 */
@Immutable
@JvmInline
private value class Anchors(
    val packedValue: Long,
)

private fun Anchors(
    collapsedHeight: Float,
    expandedHeight: Float,
) = Anchors(
    packFloats(
        val1 = collapsedHeight,
        val2 = expandedHeight,
    ),
)

private val Anchors.collapsedHeight
    get() = unpackFloat1(packedValue)


private val Anchors.expandedHeight
    get() = unpackFloat2(packedValue)

@OptIn(ExperimentalFoundationApi::class)
private fun AnchoredDraggableState<CollapsingHeaderStatus>.nestedScrollConnection() =
    object : NestedScrollConnection {
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource
        ): Offset = when (val delta = available.y) {
            in -Float.MAX_VALUE..-Float.MIN_VALUE -> dispatchRawDelta(delta).toOffset()
            else -> Offset.Zero
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset = dispatchRawDelta(delta = available.y).toOffset()

        override suspend fun onPostFling(
            consumed: Velocity,
            available: Velocity
        ): Velocity {
            settle(velocity = available.y)
            return super.onPostFling(consumed, available)
        }
    }

private fun Float.toOffset() = Offset(0f, this)