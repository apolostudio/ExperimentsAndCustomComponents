package com.ml.experimentsandtests.ui.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


/**
 * Encapsulate all transformations about showing some target (an image, relative to its center)
 * scaled and shifted in some area (a window, relative to its center)
 */
class ScalableState {
    var zoomLimits = 1.0f..5f

    private var offset by mutableStateOf(Offset.Zero)

    /**
     * Zoom of the target relative to the area size. 1.0 - the target completely fits the area.
     */
    var zoom by mutableStateOf(1f)
        private set

    private var areaSize: Size by mutableStateOf(Size.Unspecified)
    private var targetSize: Size by mutableStateOf(Size.Zero)

    /**
     * A transformation that should be applied to render the target in the area.
     *   offset - in pixels in the area coordinate system, should be applied before scaling
     *   scale - scale of the target in the area
     */
    val transformation: Transformation by derivedStateOf {
        Transformation(
            offset = offset,
            scale = zoomToScale(zoom)
        )
    }

    /**
     * The calculated base scale for 100% zoom. Calculated so that the target fits the area.
     */
    private val scaleFor100PercentZoom by derivedStateOf {
        if (targetSize.isSpecified && areaSize.isSpecified) {
            max(areaSize.width / targetSize.width, areaSize.height / targetSize.height)
        } else {
            1.0f
        }
    }

    /**
     * The calculated scale for full visibility of the target.
     */
    private val scaleForFullVisibility by derivedStateOf {
        if (targetSize.isSpecified && areaSize.isSpecified) {
            min(areaSize.width / targetSize.width, areaSize.height / targetSize.height)
        } else {
            1.0f
        }
    }

    private fun zoomToScale(zoom: Float) = zoom * scaleFor100PercentZoom

    /**
     * Limit the target center position, so:
     * - if the size of the target is less than area,
     *   the center of the target is bound to the center of the area
     * - if the size of the target is greater, then limit the center of it,
     *   so the target will be always in the area
     */
    fun limitTargetInsideArea(
        areaSize: Size,
        targetSize: Size,
    ) {
        this.areaSize = areaSize
        this.targetSize = targetSize
        zoomLimits = (scaleForFullVisibility / scaleFor100PercentZoom)..zoomLimits.endInclusive
        applyLimits()
    }

    private fun applyLimits() {
        if (targetSize.isSpecified && areaSize.isSpecified) {
            val offsetXLimits = centerLimits(targetSize.width * transformation.scale, areaSize.width)
            val offsetYLimits = centerLimits(targetSize.height * transformation.scale, areaSize.height)

            zoom = zoom.coerceIn(zoomLimits)
            offset = Offset(
                offset.x.coerceIn(offsetXLimits),
                offset.y.coerceIn(offsetYLimits),
            )
        }
    }

    private fun centerLimits(targetSize: Float, areaSize: Float): ClosedFloatingPointRange<Float> {
        val areaCenter = areaSize / 2
        val targetCenter = targetSize / 2
        val extra = (targetCenter - areaCenter).coerceAtLeast(0f)
        return -extra / 2..extra / 2
    }

    fun addPan(pan: Offset) {
        offset += pan
        applyLimits()
    }

    /**
     * @param focus on which point the camera is focused in the area coordinate system.
     * After we apply the new scale, the camera should be focused on the same point in
     * the target coordinate system.
     */
    fun addZoom(zoomMultiplier: Float, focus: Offset = Offset.Zero) {
        setZoom(zoom * zoomMultiplier, focus)
    }

    /**
     * @param focus on which point the camera is focused in the area coordinate system.
     * After we apply the new scale, the camera should be focused on the same point in
     * the target coordinate system.
     */
    fun setZoom(zoom: Float, focus: Offset = Offset.Zero) {
        val newZoom = zoom.coerceIn(zoomLimits)
        val newOffset = Transformation.offsetOf(
            point = transformation.pointOf(focus),
            transformedPoint = focus,
            scale = zoomToScale(newZoom)
        )
        this.offset = newOffset
        this.zoom = newZoom
        applyLimits()
    }

    data class Transformation(
        val offset: Offset,
        val scale: Float,
    ) {
        fun pointOf(transformedPoint: Offset) = (transformedPoint - offset) / scale

        companion object {
            // is derived from the equation `point = (transformedPoint - offset) / scale`
            fun offsetOf(point: Offset, transformedPoint: Offset, scale: Float) =
                transformedPoint - point * scale
        }
    }
}
/**
 * Initial zoom of the image. 1.0f means the image fully fits the window.
 */
private const val INITIAL_ZOOM = 1.0f

/**
 * This zoom means that the image isn't significantly zoomed for the user yet.
 */
private const val SLIGHTLY_INCREASED_ZOOM = 1.5f

@Composable
fun ScalableImage(scalableState: ScalableState, image: ImageBitmap, modifier: Modifier = Modifier) {
    BoxWithConstraints {
        val areaSize = areaSize
        val imageSize = image.size
        val imageCenter = Offset(image.width / 2f, image.height / 2f)
        val areaCenter = Offset(areaSize.width / 2f, areaSize.height / 2f)

        Box(
            modifier
                .drawWithContent {
                    drawIntoCanvas {
                        it.withSave {
                            it.translate(areaCenter.x, areaCenter.y)
                            it.translate(
                                scalableState.transformation.offset.x,
                                scalableState.transformation.offset.y
                            )
                            it.scale(
                                scalableState.transformation.scale,
                                scalableState.transformation.scale
                            )
                            it.translate(-imageCenter.x, -imageCenter.y)
                            drawImage(image)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        scalableState.addPan(pan)
                        scalableState.addZoom(zoom, centroid - areaCenter)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { position ->
                        // If a user zoomed significantly, the zoom should be the restored on double tap,
                        // otherwise the zoom should be increased
                        scalableState.setZoom(
                            if (scalableState.zoom > SLIGHTLY_INCREASED_ZOOM) {
                                INITIAL_ZOOM
                            } else {
                                scalableState.zoomLimits.endInclusive
                            },
                            position - areaCenter
                        )
                    })


                }
/*COMPOSE MP
                .onPointerEvent(PointerEventType.Scroll) {
                    val centroid = it.changes[0].position
                    val delta = it.changes[0].scrollDelta
                    val zoom = 1.2f.pow(-delta.y)
                    scalableState.addZoom(zoom, centroid - areaCenter)
                }


 */

            ,
        )

        SideEffect {
            scalableState.limitTargetInsideArea(areaSize, imageSize)
        }
    }
}

private val ImageBitmap.size get() = Size(width.toFloat(), height.toFloat())

private val BoxWithConstraintsScope.areaSize
    @Composable get() = with(LocalDensity.current) {
        Size(maxWidth.toPx(), maxHeight.toPx())
    }
