package com.jacks.moviet.sdk

import android.app.Activity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class InterstitialSessionTest {
    private val requests = mutableListOf<(InterstitialAdHandle?) -> Unit>()
    private var now = 0L
    private val session = InterstitialSession({ requests += it }, { now })

    @Test
    fun `ready ad is offered once on the first return only`() {
        launch()
        val ad = FakeAd()
        requests.single()(ad)
        assertNull(session.takeForDisplay())
        returnToApp()
        assertSame(ad, session.takeForDisplay())
        assertNull(session.takeForDisplay())
        returnToApp()
        assertNull(session.takeForDisplay())
        assertEquals(1, requests.size)
    }

    @Test
    fun `late ad is released and never shown after the eligible resume`() {
        launch()
        returnToApp()
        assertNull(session.takeForDisplay())
        val lateAd = FakeAd()
        requests.single()(lateAd)
        assertEquals(1, lateAd.releases)
        assertNull(session.takeForDisplay())
    }

    @Test
    fun `new launch loads immediately and ignores previous launch success`() {
        launch()
        close()
        launch()
        assertEquals(2, requests.size)
        val stale = FakeAd()
        requests[0](stale)
        assertEquals(1, stale.releases)
        val current = FakeAd()
        requests[1](current)
        returnToApp()
        assertSame(current, session.takeForDisplay())
    }

    @Test
    fun `previous launch failure cannot clear a new launch cache`() {
        launch()
        close()
        launch()
        val current = FakeAd()
        requests[1](current)
        requests[0](null)
        returnToApp()
        assertSame(current, session.takeForDisplay())
    }

    @Test
    fun `expired ad is released at the opportunity without a late reload`() {
        launch()
        val ad = FakeAd()
        requests.single()(ad)
        now = ad.maxAgeMillis
        returnToApp()
        assertNull(session.takeForDisplay())
        assertEquals(1, ad.releases)
        assertEquals(1, requests.size)
    }

    @Test
    fun `stopping before the eligible resume releases the ad and consumes the opportunity`() {
        launch()
        val ad = FakeAd()
        requests.single()(ad)
        returnToApp()

        session.onActivityStopped(false)

        assertEquals(1, ad.releases)
        session.onActivityStarted()
        assertNull(session.takeForDisplay())
        assertEquals(1, requests.size)
        close()
        assertEquals(1, ad.releases)
    }

    @Test
    fun `rotation retains the cache and does not advance the return policy`() {
        launch()
        val ad = FakeAd()
        requests.single()(ad)
        session.onActivityStopped(true)
        session.onActivityDestroyed(true)
        session.onActivityCreated()
        session.onActivityStarted()
        assertNull(session.takeForDisplay())
        assertEquals(0, ad.releases)
        returnToApp()
        assertSame(ad, session.takeForDisplay())
    }

    @Test
    fun `closing releases cached resources and readiness alone does not load`() {
        session.onSdkReady()
        assertEquals(0, requests.size)
        launch()
        val ad = FakeAd()
        requests.single()(ad)
        close()
        assertEquals(1, ad.releases)
        assertNull(session.takeForDisplay())
    }

    @Test
    fun `failed preload may retry during a replacement activity start`() {
        launch()
        requests.single()(null)
        session.onActivityStopped(true)
        session.onActivityDestroyed(true)
        session.onActivityCreated()
        session.onActivityStarted()
        assertEquals(2, requests.size)
        val ad = FakeAd()
        requests.last()(ad)
        returnToApp()
        assertSame(ad, session.takeForDisplay())
    }

    private fun launch() {
        session.onActivityCreated()
        session.onActivityStarted()
        session.onSdkReady()
    }

    private fun returnToApp() {
        session.onActivityStopped(false)
        session.onActivityStarted()
    }

    private fun close() {
        session.onActivityStopped(false)
        session.onActivityDestroyed(false)
    }

    private class FakeAd : InterstitialAdHandle {
        override val maxAgeMillis = 1_000L
        var releases = 0
        override fun show(activity: Activity) = error("Session must not display ads itself")
        override fun release() { releases++ }
    }
}
