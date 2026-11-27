package io.github.hiyuuna.trendtaker.cli

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrendTableTest {

    private val items = listOf(
        TrendItem(1, "octocat/hello", "https://github.com/octocat/hello", 4812, 612),
        TrendItem(2, "octocat/world", "https://github.com/octocat/world", 2390, 455),
    )

    private fun render(): String =
        Terminal(ansiLevel = AnsiLevel.NONE, width = 120).render(trendTable(items))

    @Test
    fun tableShowsRepoNamesInRankOrder() {
        val output = render()

        assertTrue(output.indexOf("octocat/hello") < output.indexOf("octocat/world"))
    }

    @Test
    fun tableShowsStarsGainAndRate() {
        val output = render()

        assertTrue(output.contains("4,812"))
        assertTrue(output.contains("+612"))
        assertTrue(output.contains("14.6%"))
    }

    @Test
    fun tableShowsUrlWithoutScheme() {
        val output = render()

        assertTrue(output.contains("github.com/octocat/hello"))
        assertTrue(!output.contains("https://"))
    }

    @Test
    fun growthRateComparesWithStarsBeforeGain() {
        assertEquals("25.0%", growthRate(stars = 500, gained = 100))
    }

    @Test
    fun growthRateIsDashWhenRepoHadNoStarsBefore() {
        assertEquals("-", growthRate(stars = 100, gained = 100))
    }
}
