package com.jacks.moviet.core.designsystem.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalSharedTransitionApi::class)
class MovieSharedContentTest {
    private val screen = Rect(0f, 0f, 360f, 800f)
    private val headerBottom = 208f
    private val detailsPoster = Rect(72f, 48f, 288f, 372f)
    private val listClip = movieListClipBounds(screen, headerBottom, listVisible = true)
    private val detailsClip = movieListClipBounds(screen, headerBottom, listVisible = false)

    @Test
    fun `fully visible destination stays whole while a large poster crosses the header`() {
        for (rowTop in listOf(headerBottom, 420f, 680f)) {
            val poster = animation(detailsPoster, Rect(20f, rowTop, 92f, rowTop + 108f))
            val clip = animation(detailsClip, listClip)
            sample(maxOf(poster.durationNanos, clip.durationNanos)) { time ->
                val posterBounds = poster.getValueFromNanos(time)
                val clipBounds = clip.getValueFromNanos(time)
                assertTrue("Top cropped at $time ns for row $rowTop", clipBounds.top <= posterBounds.top + 0.5f)
                assertTrue("Bottom cropped at $time ns for row $rowTop", clipBounds.bottom >= posterBounds.bottom - 0.5f)
            }
        }
    }

    @Test
    fun `partly hidden destination crops gradually into the final visible part of the row`() {
        val row = Rect(20f, headerBottom - 54f, 92f, headerBottom + 54f)
        val poster = animation(detailsPoster, row)
        val clip = animation(detailsClip, listClip)
        val duration = maxOf(poster.durationNanos, clip.durationNanos)
        var hasIntermediateCrop = false
        sample(duration) { time ->
            val crop = (clip.getValueFromNanos(time).top - poster.getValueFromNanos(time).top).coerceAtLeast(0f)
            if (crop > 0f && crop < 54f) hasIntermediateCrop = true
        }
        assertEquals(0f, (detailsClip.top - detailsPoster.top).coerceAtLeast(0f), 0f)
        assertTrue("Crop should develop over the animation", hasIntermediateCrop)
        assertEquals(54f, clip.getValueFromNanos(duration).top - poster.getValueFromNanos(duration).top, 0.5f)
    }

    @Test
    fun `reversing a partial return expands the crop back to the full details poster`() {
        val row = Rect(20f, headerBottom - 54f, 92f, headerBottom + 54f)
        val returningPoster = animation(detailsPoster, row)
        val returningClip = animation(detailsClip, listClip)
        val reversalTime = 150_000_000L
        val interruptedPoster = returningPoster.getValueFromNanos(reversalTime)
        val interruptedClip = returningClip.getValueFromNanos(reversalTime)
        val poster = animation(interruptedPoster, detailsPoster)
        val clip = animation(interruptedClip, detailsClip)
        val duration = maxOf(poster.durationNanos, clip.durationNanos)
        val initialCrop = (interruptedClip.top - interruptedPoster.top).coerceAtLeast(0f)
        assertTrue(initialCrop > 0f)
        assertEquals(interruptedClip, clip.getValueFromNanos(0L))
        sample(duration) { time ->
            val crop = (clip.getValueFromNanos(time).top - poster.getValueFromNanos(time).top).coerceAtLeast(0f)
            assertTrue("Reversal should reveal the poster", crop <= initialCrop + 0.5f)
        }
        assertEquals(detailsClip, clip.getValueFromNanos(duration))
        assertEquals(detailsPoster, poster.getValueFromNanos(duration))
    }

    private fun animation(start: Rect, end: Rect) = TargetBasedAnimation(
        animationSpec = MovieBoundsTransform.createAnimationSpec(start, end),
        typeConverter = Rect.VectorConverter,
        initialValue = start,
        targetValue = end,
    )

    private fun sample(durationNanos: Long, block: (Long) -> Unit) {
        for (frame in 0..120) block(durationNanos * frame / 120)
    }
}
