package io.github.hiyuuna.trendtaker.collect

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

enum class CollectStatus { RUNNING, SUCCEEDED, PARTIAL, FAILED }

@Entity
@Table(name = "collect_runs")
class CollectRun(
    val startedAt: Instant,
    @Enumerated(EnumType.STRING)
    var status: CollectStatus = CollectStatus.RUNNING,
    var finishedAt: Instant? = null,
    var discovered: Int? = null,
    var tracked: Int? = null,
    var failed: Int? = null,
    var stoppedByRateLimit: Boolean? = null,
    var error: String? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)
