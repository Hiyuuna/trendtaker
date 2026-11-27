package io.github.hiyuuna.trendtaker

import io.github.hiyuuna.trendtaker.collect.CollectRun
import io.github.hiyuuna.trendtaker.collect.CollectRunRepository
import io.github.hiyuuna.trendtaker.collect.CollectStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class StatusResponse(
    val name: String,
    val version: String,
    val time: Instant,
    val lastCollect: LastCollect?,
)

data class LastCollect(
    val status: CollectStatus,
    val startedAt: Instant,
    val finishedAt: Instant?,
    val discovered: Int?,
    val tracked: Int?,
    val failed: Int?,
)

@RestController
class StatusController(private val collectRunRepository: CollectRunRepository) {

    @GetMapping("/status")
    fun status(): StatusResponse =
        StatusResponse(
            name = "trendtaker",
            version = "0.1.0",
            time = Instant.now(),
            lastCollect = collectRunRepository.findTopByOrderByStartedAtDesc()?.toLastCollect(),
        )

    private fun CollectRun.toLastCollect() =
        LastCollect(status, startedAt, finishedAt, discovered, tracked, failed)
}
