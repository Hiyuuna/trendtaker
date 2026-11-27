package io.github.hiyuuna.trendtaker.cli

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.table
import java.util.Locale

val gainColor = TextColors.rgb("#3DF72F")
val dimColor = TextColors.gray

fun trendTable(items: List<TrendItem>): Widget = table {
    borderStyle = dimColor
    column(0) { align = TextAlign.RIGHT }
    column(2) { align = TextAlign.RIGHT }
    column(3) { align = TextAlign.RIGHT }
    column(4) { align = TextAlign.RIGHT }
    header {
        style = dimColor
        row("#", "리포", "별", "증가", "증가율", "주소")
    }
    body {
        items.forEach { item ->
            row(
                item.rank,
                item.fullName,
                formatCount(item.stars),
                gainColor("+${formatCount(item.gained)}"),
                gainColor(growthRate(item.stars, item.gained)),
                dimColor(shortUrl(item.htmlUrl)),
            )
        }
    }
}

fun formatCount(value: Int): String = String.format(Locale.US, "%,d", value)

fun growthRate(stars: Int, gained: Int): String {
    val before = stars - gained
    if (before <= 0) return "-"
    return String.format(Locale.US, "%.1f%%", gained * 100.0 / before)
}

fun shortUrl(url: String): String = url.removePrefix("https://").removePrefix("http://")
