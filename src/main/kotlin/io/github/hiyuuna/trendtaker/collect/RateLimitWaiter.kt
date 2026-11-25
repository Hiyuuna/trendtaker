package io.github.hiyuuna.trendtaker.collect

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class RateLimitWaiter(
    @Value("\${collect.max-rate-limit-wait-minutes:15}") private val maxWaitMinutes: Long,
) {
    fun waitUntil(resetAt: Instant): Boolean {
        val wait = Duration.between(Instant.now(), resetAt).plusSeconds(1)
        if (wait > Duration.ofMinutes(maxWaitMinutes)) return false
        if (!wait.isNegative) Thread.sleep(wait.toMillis())
        return true
    }
}
