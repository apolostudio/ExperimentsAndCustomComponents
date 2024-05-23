package com.ml.experimentsandtests.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.UUID

private val products = listOf(
    Product("🍎", "Apples"),
    Product("🍪", "Cookies"),
    Product("🍉", "Watermelon"),
    Product("🫐", "Blueberries"),
    Product("🍊", "Oranges"),
    Product("🍑", "Peaches"),
    Product("🥦", "Broccoli"),
)
@OptIn(ExperimentalSharedTransitionApi::class)
private val boundsTransition = BoundsTransform { _, _ -> spring(dampingRatio = Spring.DampingRatioLowBouncy) }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTranstionDemo(modifier: Modifier = Modifier) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        SharedTransitionLayout {
            var visibleDetails by remember { mutableStateOf<Product?>(null) }
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductInList(product = product, visible = visibleDetails != product) {
                        visibleDetails = product
                    }
                }
            }

            ProductInOverlay(
                product = visibleDetails
            ) {
                visibleDetails = null
            }

            BackHandler(visibleDetails != null) {
                visibleDetails = null
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ProductInList(
    product: Product,
    visible: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var height by remember { mutableStateOf<Dp?>(null) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .onSizeChanged {
                height = density.run { it.height.toDp() }
            }
    ) {
        // Since the item disappears after the animation has finished, it would disappear from the list. We want to remember how high it was, to reserve teh space
        // Not sure how failsafe this is
        height?.let {
            Spacer(modifier = Modifier.height(it))
        }

        AnimatedVisibility(
            visible = visible
        ) {
            Box(
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "${product.id}_bounds"),
                        animatedVisibilityScope = this,
                        boundsTransform = boundsTransition,
                    )
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Item(
                    product = product,
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState(key = product.id),
                        animatedVisibilityScope = this@AnimatedVisibility,
                        boundsTransform = boundsTransition,
                    ),
                    onClick = onClick
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ProductInOverlay(
    product: Product?,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    AnimatedContent(
        modifier = modifier,
        transitionSpec = {
            // fade the scrim
            fadeIn() togetherWith fadeOut()
        },
        targetState = product,
    ) { product1 ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (product1 == null) return@AnimatedContent

            ScrimOverlay(
                onDismissRequest = onDismissRequest
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "${product1.id}_bounds"),
                        animatedVisibilityScope = this@AnimatedContent,
                        boundsTransform = boundsTransition,
                    )
                    //.shadow(4.dp, RoundedCornerShape(12.dp)) // TODO Shadow is clipped during transition. I tried to return a null path in clipInOverlayDuringTransition to disable clipping in the overlay altogether, did not work
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Item(
                    product = product1,
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState(key = product1.id),
                        animatedVisibilityScope = this@AnimatedContent,
                        boundsTransform = boundsTransition,
                    )
                ) {
                    // Nothing to do
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { /*TODO*/ }) {
                        Text(text = "Do things")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Item(
    product: Product,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                   // onLongClick = { onClick() },
                    onClick = {  onClick()}
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = product.emoji, style = MaterialTheme.typography.titleLarge)
            }
            Text(modifier = Modifier.weight(1f), text = product.name, style = MaterialTheme.typography.bodyLarge)
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    text = "1kg",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun ScrimOverlay(
    onDismissRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.20f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismissRequest
            ),
    )
}

data class Product(
    val emoji: String,
    val name: String,
    val id: String = UUID.randomUUID().toString(),
)