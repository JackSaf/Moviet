package com.jacks.moviet.sdk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecondVisitPolicyTest {
    @Test
    fun `only the first return triggers within a launch session`() {
        val policy = SecondVisitPolicy()
        assertFalse(policy.onForeground()) // Launch.
        assertTrue(policy.onForeground()) // First return.
        repeat(10) { assertFalse(policy.onForeground()) }
        assertTrue(policy.isConsumed)
    }

    @Test
    fun `new process starts a fresh session without saved counters`() {
        repeat(3) {
            val policy = SecondVisitPolicy()
            assertFalse(policy.isConsumed)
            assertFalse(policy.onForeground())
            assertTrue(policy.onForeground())
            assertFalse(policy.onForeground())
        }
    }

    @Test
    fun `closing the last activity resets even if the process survives`() {
        val policy = SecondVisitPolicy()
        repeat(3) {
            assertFalse(policy.onForeground())
            assertTrue(policy.onForeground())
            assertFalse(policy.onForeground())
            policy.reset()
            assertFalse(policy.isConsumed)
        }
    }

    @Test
    fun `closing before the first return does not make relaunch eligible`() {
        val policy = SecondVisitPolicy()
        assertFalse(policy.onForeground())
        policy.reset()
        assertFalse(policy.onForeground())
        assertTrue(policy.onForeground())
    }
}
