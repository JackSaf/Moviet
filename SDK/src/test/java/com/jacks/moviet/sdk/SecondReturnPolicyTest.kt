package com.jacks.moviet.sdk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecondReturnPolicyTest {
    @Test
    fun `only the second return triggers and it stays consumed across instances`() {
        val store = MemoryStore()
        val policy = SecondReturnPolicy(store)
        assertFalse(policy.onForeground()) // launch
        assertFalse(policy.onForeground()) // first return
        assertTrue(SecondReturnPolicy(store).onForeground()) // second return after process recreation
        assertFalse(SecondReturnPolicy(store).onForeground())
    }

    private class MemoryStore : SecondReturnPolicy.VisitStore {
        override var foregroundCount = 0
        override var consumed = false
    }
}
