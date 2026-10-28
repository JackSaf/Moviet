package com.jacks.moviet.sdk

/** Converts Activity callbacks into app foregrounds without counting rotations or overlapping activities. */
internal class ForegroundTracker {
    private var startedActivityCount = 0
    private var isRecreatingActivity = false

    val isInForeground: Boolean get() = startedActivityCount > 0

    fun onActivityStarted(): Boolean {
        startedActivityCount++
        if (startedActivityCount != 1) return false
        val isForeground = !isRecreatingActivity
        isRecreatingActivity = false
        return isForeground
    }

    fun onActivityStopped(isChangingConfigurations: Boolean) {
        check(startedActivityCount > 0) { "Activity stopped without a matching start" }
        startedActivityCount--
        if (startedActivityCount == 0) isRecreatingActivity = isChangingConfigurations
    }
}
