package com.jacks.moviet.search.presentation.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchAdPositionsTest {
    @Test
    fun `three movies are interleaved without changing their indexes`() {
        assertEquals(5, SearchAdPositions.itemCount(movieCount = 3, adsEnabled = true))
        assertEquals(listOf(0, 1, 2), listOf(0, 2, 4).map(SearchAdPositions::movieIndex))
        assertTrue(SearchAdPositions.isAd(1))
        assertTrue(SearchAdPositions.isAd(3))
        assertFalse(SearchAdPositions.isAd(4))
    }

    @Test
    fun `short result lists have no ad slots`() {
        assertEquals(2, SearchAdPositions.itemCount(movieCount = 2, adsEnabled = true))
        assertEquals(3, SearchAdPositions.itemCount(movieCount = 3, adsEnabled = false))
    }
}
