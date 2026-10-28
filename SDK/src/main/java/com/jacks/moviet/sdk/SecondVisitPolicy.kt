package com.jacks.moviet.sdk

/** The initial foreground is visit one; the first return is visit two. */
internal class SecondVisitPolicy(
    private val store: VisitStore,
) {
    val isConsumed: Boolean get() = store.foregroundCount >= SECOND_VISIT_FOREGROUND

    fun onForeground(): Boolean {
        if (isConsumed) return false
        // One persisted value avoids a partially updated count/consumed pair.
        store.foregroundCount = store.foregroundCount.coerceAtLeast(0) + 1
        return isConsumed
    }

    interface VisitStore {
        var foregroundCount: Int
    }
}

private const val SECOND_VISIT_FOREGROUND = 2
