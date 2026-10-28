package com.jacks.moviet.search.presentation.components

/** Native ads occupy zero-based visible positions one and three. */
internal object SearchAdPositions {
    fun isAd(index: Int): Boolean = index == 1 || index == 3

    fun movieIndex(index: Int): Int = index - (if (index > 1) 1 else 0) - (if (index > 3) 1 else 0)

    fun itemCount(movieCount: Int, adsEnabled: Boolean): Int =
        movieCount + if (adsEnabled && movieCount >= 3) 2 else 0
}
