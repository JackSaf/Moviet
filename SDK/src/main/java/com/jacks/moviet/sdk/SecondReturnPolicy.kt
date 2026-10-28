package com.jacks.moviet.sdk

/** The initial foreground is a launch; the next two are returns. */
internal class SecondReturnPolicy(
    private val store: VisitStore,
) {
    fun onForeground(): Boolean {
        if (store.consumed) return false
        store.foregroundCount += 1
        if (store.foregroundCount != SECOND_RETURN_FOREGROUND) return false
        store.consumed = true
        return true
    }

    interface VisitStore {
        var foregroundCount: Int
        var consumed: Boolean
    }
}

private const val SECOND_RETURN_FOREGROUND = 3
