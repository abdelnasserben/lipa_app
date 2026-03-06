package com.kori.app.core.model.common

data class CursorPage(
    val nextCursor: String?,
    val hasMore: Boolean,
)

data class CursorPagedResponse<T>(
    val items: List<T>,
    val page: CursorPage,
)