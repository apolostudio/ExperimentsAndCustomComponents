package com.ml.experimentsandtests

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.ml.experimentsandtests.ui.components.AndroidExternalSurfaceColors
import com.ml.experimentsandtests.ui.components.NestedScrollWithCollapsibleHeader
import com.ml.experimentsandtests.ui.components.NestedSharedBoundsSample
import com.ml.experimentsandtests.ui.components.RefreshPage
import com.ml.experimentsandtests.ui.components.SharedElementInAnimatedContentSample
import com.ml.experimentsandtests.ui.components.SharedElementWithFABInOverlaySample
import com.ml.experimentsandtests.ui.components.SharedElementWithMovableContentSample
import com.ml.experimentsandtests.ui.components.SharedTranstionDemo
import com.ml.experimentsandtests.ui.theme.ExpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        super.onCreate(savedInstanceState)
        setContent {
            ExpTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {


                    HorizontalPager(state = rememberPagerState(0, pageCount = {8})) { page ->

                            when (page) {
                                0 -> {
                                    RefreshPage()
                                }
                                1 -> {
                                    NestedScrollWithCollapsibleHeader()
                                }
                                2 -> {
                                    SharedTranstionDemo()
                                }
                                3 -> {
                                    NestedSharedBoundsSample()
                                }
                                4 -> {
                                    SharedElementWithMovableContentSample()
                                }
                                5 -> {
                                    SharedElementWithFABInOverlaySample()
                                }
                                6 -> {
                                    SharedElementInAnimatedContentSample()
                                }
                                7 -> {
                                AndroidExternalSurfaceColors()
                            }
                            }


                    }


                }
            }
        }
    }
}
