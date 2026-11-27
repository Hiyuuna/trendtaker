package io.github.hiyuuna.trendtaker.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.coroutines.runBlocking

class Trendtaker : CliktCommand(name = "trendtaker") {
    override fun run() = Unit
}

class Trending : CliktCommand(name = "trending") {

    private val limit by option("--limit", help = "보여줄 리포 수").int().default(20)

    private val server by option("--server", envvar = "TRENDTAKER_SERVER", help = "trendtaker 서버 주소")
        .default("http://localhost:8080")

    override fun run() {
        val response = try {
            runBlocking { TrendClient(server).trending(limit) }
        } catch (e: Exception) {
            throw CliktError("서버에서 순위를 가져오지 못했습니다: $server (${e.message})")
        }

        val terminal = Terminal()
        if (response.items.isEmpty()) {
            terminal.println(dimColor("아직 비교할 수집 기록이 없습니다."))
            return
        }
        terminal.println(dimColor("${response.from} → ${response.to}"))
        terminal.println(trendTable(response.items))
    }
}

fun main(args: Array<String>) = Trendtaker().subcommands(Trending()).main(args)
