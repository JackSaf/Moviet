package com.jacks.moviet.sdk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundTrackerTest {
    @Test
    fun `launch and background returns count once each`() {
        val tracker = ForegroundTracker()
        assertTrue(tracker.onActivityStarted())
        tracker.onActivityStopped(isChangingConfigurations = false)
        assertTrue(tracker.onActivityStarted())
    }

    @Test
    fun `rotations do not count but the next real return does`() {
        val tracker = ForegroundTracker()
        assertTrue(tracker.onActivityStarted())
        repeat(3) {
            tracker.onActivityStopped(isChangingConfigurations = true)
            assertFalse(tracker.onActivityStarted())
        }
        tracker.onActivityStopped(isChangingConfigurations = false)
        assertTrue(tracker.onActivityStarted())
    }

    @Test
    fun `overlapping activities remain one foreground until all stop`() {
        val tracker = ForegroundTracker()
        assertTrue(tracker.onActivityStarted())
        assertFalse(tracker.onActivityStarted())
        tracker.onActivityStopped(isChangingConfigurations = false)
        assertFalse(tracker.onActivityStarted())
        tracker.onActivityStopped(isChangingConfigurations = false)
        tracker.onActivityStopped(isChangingConfigurations = false)
        assertTrue(tracker.onActivityStarted())
    }

    @Test
    fun `configuration change of one overlapping activity does not suppress a real return`() {
        val tracker = ForegroundTracker()
        assertTrue(tracker.onActivityStarted())
        assertFalse(tracker.onActivityStarted())
        tracker.onActivityStopped(isChangingConfigurations = true)
        tracker.onActivityStopped(isChangingConfigurations = false)
        assertTrue(tracker.onActivityStarted())
    }

    @Test
    fun `second visit policy ignores rotations and in-app activities`() {
        val tracker = ForegroundTracker()
        val policy = SecondVisitPolicy()
        fun start(): Boolean = tracker.onActivityStarted() && policy.onForeground()
        assertFalse(start()) // Launch.
        tracker.onActivityStopped(isChangingConfigurations = true)
        assertFalse(start()) // Rotation.
        assertFalse(start()) // Another in-app activity.
        tracker.onActivityStopped(isChangingConfigurations = false)
        tracker.onActivityStopped(isChangingConfigurations = false)
        assertTrue(start()) // First return is the second visit.
        tracker.onActivityStopped(isChangingConfigurations = false)
        assertFalse(start()) // Already consumed.
    }
}
