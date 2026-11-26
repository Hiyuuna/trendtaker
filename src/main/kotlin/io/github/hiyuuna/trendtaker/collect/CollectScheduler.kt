package io.github.hiyuuna.trendtaker.collect

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CollectScheduler(private val collectService: CollectService) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${collect.cron}", zone = "UTC")
    fun run() {
        log.info("collect started")
        try {
            val result = collectService.collect()
            if (result == null) {
                log.warn("collect skipped: already running")
                return
            }
            log.info(
                "collect finished: discovered={}, tracked={}, inactive={}, failed={}, discoverError={}, stoppedByRateLimit={}",
                result.discovered, result.tracked, result.inactive, result.failed, result.discoverError, result.stoppedByRateLimit,
            )
        } catch (e: Exception) {
            log.error("collect failed", e)
        }
    }
}
