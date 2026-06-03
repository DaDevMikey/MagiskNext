package com.topjohnwu.magisk.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.OverscrollFactory
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

@OptIn(ExperimentalFoundationApi::class)
class BouncyOverscrollEffect(private val scope: CoroutineScope) : OverscrollEffect {
    private var stretch by mutableFloatStateOf(0f)
    private var isAnimating by mutableStateOf(false)

    override val isInProgress: Boolean
        get() = stretch != 0f || isAnimating

    override val effectModifier: Modifier = Modifier.graphicsLayer {
        translationY = stretch
        clip = false
    }

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset
    ): Offset {
        var unStretchConsumed = Offset.Zero
        
        if (stretch != 0f) {
            val stretchSign = sign(stretch)
            val isScrollingBack = sign(delta.y) != stretchSign
            if (isScrollingBack) {
                val amountToUnstretch = if (abs(delta.y) > abs(stretch)) -stretch else delta.y
                stretch += amountToUnstretch
                unStretchConsumed = Offset(0f, amountToUnstretch)
            }
        }

        val remainingDelta = delta - unStretchConsumed
        val consumedByScroll = performScroll(remainingDelta)
        val overscroll = remainingDelta - consumedByScroll

        val isDrag = source == NestedScrollSource.UserInput ||
                     source.toString().contains("UserInput") ||
                     source.toString().contains("Drag") ||
                     (try { source == NestedScrollSource.Drag } catch(e: Throwable) { false })
        if (isDrag && overscroll.y != 0f) {
            val tension = 0.45f // more bouncy
            stretch += overscroll.y * tension
            return unStretchConsumed + consumedByScroll + overscroll
        }
        
        return unStretchConsumed + consumedByScroll
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity
    ) {
        val consumed = performFling(velocity)
        val remaining = velocity - consumed

        if (remaining.y != 0f || stretch != 0f) {
            isAnimating = true
            val animatable = Animatable(stretch)
            
            // If there's remaining velocity, we use it to fling the bounce
            val initialVel = if (remaining.y != 0f) remaining.y * 0.5f else 0f
            
            animatable.animateTo(
                targetValue = 0f,
                initialVelocity = initialVel,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) {
                stretch = this.value
            }
            isAnimating = false
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
class BouncyOverscrollFactory(private val scope: CoroutineScope) : OverscrollFactory {
    override fun createOverscrollEffect(): OverscrollEffect {
        return BouncyOverscrollEffect(scope)
    }

    override fun equals(other: Any?): Boolean {
        return other is BouncyOverscrollFactory
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
