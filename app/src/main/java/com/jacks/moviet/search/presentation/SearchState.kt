package com.jacks.moviet.search.presentation

import com.jacks.moviet.R
import com.jacks.moviet.core.presentation.util.UiText

data class SearchState(
    val welcome: UiText = UiText.StringResource(R.string.search_welcome),
)
