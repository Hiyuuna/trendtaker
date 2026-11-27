package io.github.hiyuuna.trendtaker.trend

import java.time.LocalDate

data class TrendResponse(
    val from: LocalDate?,
    val to: LocalDate?,
    val items: List<TrendItem>,
)

data class TrendItem(
    val rank: Int,
    val fullName: String,
    val htmlUrl: String,
    val stars: Int,
    val gained: Int,
)
