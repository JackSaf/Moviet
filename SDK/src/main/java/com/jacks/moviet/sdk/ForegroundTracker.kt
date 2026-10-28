package com.jacks.moviet.sdk

/** Converts Activity callbacks into app foregrounds without counting rotations or overlapping activities. */
internal class ForegroundTracker {
    private var startedActivities = 0
    private var changingConfigurations = false

    fun onActivityStarted(): Boolean {
        if (startedActivities++ != 0) return false
        val isForeground = !changingConfigurations
        changingConfigurations = false
        return isForeground
    }

    fun onActivityStopped(isChangingConfigurations: Boolean) {
        check(startedActivities > 0) { "Activity stopped without a matching start" }
        startedActivities--
        if (startedActivities == 0) changingConfigurations = isChangingConfigurations
    }
}
