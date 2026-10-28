package com.jacks.moviet.sdk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class SecondVisitPolicyTest {
    @Test
    fun `first return triggers the second visit and stays consumed across instances`() {
        val store = MemoryStore()
        val policy = SecondVisitPolicy(store)
        assertFalse(policy.onForeground()) // launch
        assertTrue(SecondVisitPolicy(store).onForeground()) // first return after process recreation
        assertFalse(SecondVisitPolicy(store).onForeground())
        assertEquals(2, store.foregroundCount)
    }

    @Test
    fun `consumed counters including the old third-visit policy never trigger again`() {
        for (count in listOf(2, 3)) {
            val store = MemoryStore().apply { foregroundCount = count }
            val policy = SecondVisitPolicy(store)
            assertTrue(policy.isConsumed)
            repeat(10) { assertFalse(policy.onForeground()) }
            assertEquals(count, store.foregroundCount)
        }
    }

    private class MemoryStore : SecondVisitPolicy.VisitStore {
        override var foregroundCount = 0
    }
}
