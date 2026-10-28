package com.jacks.moviet.sdk

/** One opportunity per launch session: initial foreground, then first return. */
internal class SecondVisitPolicy {
    private var foregroundCount = 0

    val isConsumed: Boolean get() = foregroundCount >= SECOND_VISIT_FOREGROUND

    fun onForeground(): Boolean {
        if (isConsumed) return false
        foregroundCount++
        return isConsumed
    }

    fun reset() {
        foregroundCount = 0
    }
}

private const val SECOND_VISIT_FOREGROUND = 2
