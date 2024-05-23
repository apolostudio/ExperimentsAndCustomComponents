package com.ml.experimentsandtests.ui.components

import android.graphics.Rect
import androidx.compose.foundation.AndroidExternalSurface
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun AndroidExternalSurfaceColors() {
    AndroidExternalSurface(
        modifier = Modifier.fillMaxWidth().height(400.dp)
    ) {
        // Resources can be initialized/cached here

        // A surface is available, we can start rendering
        onSurface { surface, width, height ->
            var w = width
            var h = height

            // Initial draw to avoid a black frame
            surface.lockCanvas(android.graphics.Rect(0, 0, w, h)).apply {
                drawColor(Color.Blue.toArgb())
                surface.unlockCanvasAndPost(this)
            }

            // React to surface dimension changes
            surface.onChanged { newWidth, newHeight ->
                w = newWidth
                h = newHeight
            }

            // Cleanup if needed
            surface.onDestroyed {
            }

            // Render loop, automatically cancelled on surface destruction
            while (true) {
                withFrameNanos { time ->
                    surface.lockCanvas(Rect(0, 0, w, h)).apply {
                        val timeMs = time / 1_000_000L
                        val t = 0.5f + 0.5f * sin(timeMs / 1_000.0f)
                        drawColor(lerp(Color.Blue, Color.Green, t).toArgb())
                        surface.unlockCanvasAndPost(this)
                    }
                }
            }
        }
    }
}