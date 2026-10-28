package com.jacks.moviet.sdk

/** Main-thread state machine, independent of Android callbacks and any advertising vendor. */
internal class InterstitialSession(
    private val load: ((InterstitialAdHandle?) -> Unit) -> Unit,
    private val nowMillis: () -> Long,
) {
    private val foregrounds = ForegroundTracker()
    private val visitPolicy = SecondVisitPolicy()
    private var createdActivityCount = 0
    private var isSdkReady = false
    private var isLoading = false
    private var generation = 0L
    private var cachedAd: InterstitialAdHandle? = null
    private var loadedAtMillis = 0L
    private var shouldShowOnResume = false

    fun onSdkReady() {
        isSdkReady = true
        preload()
    }

    fun onActivityCreated() { createdActivityCount++ }

    fun onActivityStarted() {
        if (foregrounds.onActivityStarted()) shouldShowOnResume = visitPolicy.onForeground()
        preload()
    }

    fun takeForDisplay(): InterstitialAdHandle? {
        if (!shouldShowOnResume) return null
        shouldShowOnResume = false
        val ad = cachedAd
        cachedAd = null
        if (ad != null && isFresh(ad)) return ad
        ad?.release()
        return null
    }

    fun onActivityStopped(isChangingConfigurations: Boolean) {
        foregrounds.onActivityStopped(isChangingConfigurations)
        if (!foregrounds.isInForeground && !isChangingConfigurations) {
            shouldShowOnResume = false
            if (visitPolicy.isConsumed) clearCache()
        }
    }

    fun onActivityDestroyed(isChangingConfigurations: Boolean) {
        createdActivityCount--
        if (createdActivityCount != 0 || isChangingConfigurations) return
        // Invalidate in-flight callbacks before the next launch can start another load.
        generation++
        isLoading = false
        visitPolicy.reset()
        shouldShowOnResume = false
        clearCache()
    }

    private fun preload() {
        if (!isSdkReady || isLoading || createdActivityCount == 0 || visitPolicy.isConsumed) return
        if (cachedAd?.let(::isFresh) == true) return
        clearCache()
        isLoading = true
        val requestGeneration = generation
        load { ad ->
            if (requestGeneration != generation) {
                ad?.release()
                return@load
            }
            isLoading = false
            if (createdActivityCount == 0 || visitPolicy.isConsumed) {
                ad?.release()
            } else {
                cachedAd = ad
                loadedAtMillis = nowMillis()
            }
        }
    }

    private fun isFresh(ad: InterstitialAdHandle): Boolean = nowMillis() - loadedAtMillis < ad.maxAgeMillis

    private fun clearCache() {
        cachedAd?.release()
        cachedAd = null
    }
}
