package io.github.hiyuuna.trendtaker.collect

import org.springframework.data.jpa.repository.JpaRepository

interface CollectRunRepository : JpaRepository<CollectRun, Long> {
    fun findTopByOrderByStartedAtDesc(): CollectRun?
}
