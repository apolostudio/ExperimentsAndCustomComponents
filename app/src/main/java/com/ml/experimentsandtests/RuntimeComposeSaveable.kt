package com.ml.experimentsandtests

import android.net.Uri
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.ui.graphics.Color

val ColorSaver = run {
    val redKey = "Red"
    val greenKey = "Green"
    val blueKey = "Blue"
    val alphaKey = "Alpha"
    mapSaver(
        save = { mapOf(redKey to it.red, greenKey to it.green, blueKey to it.blue, alphaKey to it.alpha) },
        restore = {
            Color(
                red = it[redKey] as Float,
                green = it[greenKey] as Float,
                blue = it[blueKey] as Float,
                alpha = it[alphaKey] as Float
            )
        }
    )
}


val UriSaver: Saver<Uri, String> = Saver(
    save = { uri -> uri.toString() },
    restore ={ string -> Uri.parse(string) }
)

val UriListSaver: Saver<List<Uri>, List<String>> = Saver(
    save = { uriList -> uriList.map { it.toString() } },
    restore = { stringList -> stringList.map { Uri.parse(it) } }
)