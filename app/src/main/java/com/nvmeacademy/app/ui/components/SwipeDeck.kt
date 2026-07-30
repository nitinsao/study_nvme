package com.nvmeacademy.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

/**
 * Applies a Tinder-style card-stack look to a HorizontalPager page: the
 * current card is full size/opacity, and neighboring cards shrink and fade
 * as they scroll away, so swiping reads as flipping through a deck rather
 * than a flat carousel.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pagerCardTransform(pagerState: PagerState, page: Int): Modifier {
    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
    val closeness = 1f - pageOffset.coerceIn(0f, 1f)
    val scaleAmount = 0.88f + (1f - 0.88f) * closeness
    val alphaAmount = 0.4f + (1f - 0.4f) * closeness
    return this.scale(scaleAmount).alpha(alphaAmount)
}

/** A slim "position N of Total" indicator shown under a swipeable deck. */
@Composable
fun DeckPositionIndicator(current: Int, total: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        LinearProgressIndicator(
            progress = if (total > 0) current.toFloat() / total else 0f,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "$current of $total",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
