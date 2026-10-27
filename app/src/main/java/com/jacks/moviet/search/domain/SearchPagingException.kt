package com.jacks.moviet.search.domain

import com.jacks.moviet.core.domain.NetworkError

/** Preserves the repository failure in Paging's LoadState.Error. */
class SearchPagingException(val error: NetworkError) : Exception()
