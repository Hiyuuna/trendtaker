package io.github.hiyuuna.trendtaker.collect

import io.github.hiyuuna.trendtaker.github.GitHubClient
import io.github.hiyuuna.trendtaker.github.GitHubRateLimitException
import io.github.hiyuuna.trendtaker.github.GitHubRepo
import io.github.hiyuuna.trendtaker.repo.Repo
import io.github.hiyuuna.trendtaker.repo.RepoRepository
import io.github.hiyuuna.trendtaker.repo.RepoSnapshot
import io.github.hiyuuna.trendtaker.repo.RepoSnapshotRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicBoolean

data class CollectResult(
    val discovered: Int,
    val tracked: Int,
    val failed: Int,
    val discoverError: String? = null,
    val stoppedByRateLimit: Boolean = false,
    val inactive: Int = 0,
)

@Service
class CollectService(
    private val gitHubClient: GitHubClient,
    private val repoRepository: RepoRepository,
    private val snapshotRepository: RepoSnapshotRepository,
    private val rateLimitWaiter: RateLimitWaiter,
    private val collectRunRepository: CollectRunRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val running = AtomicBoolean(false)

    fun collect(now: Instant = Instant.now()): CollectResult? {
        if (!running.compareAndSet(false, true)) return null
        val run = collectRunRepository.save(CollectRun(startedAt = now))
        try {
            val today = LocalDate.ofInstant(now, ZoneOffset.UTC)

            var discoverError: String? = null
            val discovered = try {
                discover(today, now)
            } catch (e: Exception) {
                log.warn("discover failed", e)
                discoverError = e.message ?: e.javaClass.simpleName
                0
            }

            val track = track(today, now)
            val result = CollectResult(
                discovered, track.tracked, track.failed, discoverError, track.stoppedByRateLimit, track.inactive,
            )
            finish(run, result)
            return result
        } catch (e: Exception) {
            run.status = CollectStatus.FAILED
            run.finishedAt = Instant.now()
            run.error = shorten(e.message ?: e.javaClass.simpleName)
            collectRunRepository.save(run)
            throw e
        } finally {
            running.set(false)
        }
    }

    private fun discover(today: LocalDate, now: Instant): Int {
        val query = "created:>${today.minusDays(DISCOVER_DAYS)} stars:>$DISCOVER_MIN_STARS"
        var discovered = 0
        for (item in gitHubClient.searchRepositories(query, 100).items) {
            if (repoRepository.existsById(item.id)) continue
            repoRepository.save(
                Repo(
                    id = item.id,
                    fullName = item.fullName,
                    description = item.description,
                    language = item.language,
                    htmlUrl = item.htmlUrl,
                    topics = item.topics,
                    fork = item.fork,
                    archived = item.archived,
                    createdAt = item.createdAt,
                    firstSeenAt = now,
                    updatedAt = now,
                )
            )
            discovered++
        }
        return discovered
    }

    private class TrackResult(
        var tracked: Int = 0,
        var failed: Int = 0,
        var inactive: Int = 0,
        var stoppedByRateLimit: Boolean = false,
    )

    private fun track(today: LocalDate, now: Instant): TrackResult {
        val result = TrackResult()
        for (repo in repoRepository.findAll()) {
            if (snapshotRepository.findByRepoIdAndCapturedOn(repo.id, today) != null) continue
            if (isInactive(repo, today, now)) {
                result.inactive++
                continue
            }
            try {
                val item = fetchRepository(repo.id) ?: continue

                repo.fullName = item.fullName
                repo.description = item.description
                repo.language = item.language
                repo.htmlUrl = item.htmlUrl
                repo.topics = item.topics
                repo.fork = item.fork
                repo.archived = item.archived
                repo.updatedAt = now
                repoRepository.save(repo)

                snapshotRepository.save(
                    RepoSnapshot(
                        repoId = repo.id,
                        capturedOn = today,
                        stars = item.stargazersCount,
                        forks = item.forksCount,
                        openIssues = item.openIssuesCount,
                        capturedAt = now,
                    )
                )
                result.tracked++
            } catch (e: GitHubRateLimitException) {
                log.warn("rate limit reached until {}, stopping track", e.resetAt)
                result.stoppedByRateLimit = true
                break
            } catch (e: Exception) {
                log.warn("track failed for repo {}", repo.id, e)
                result.failed++
            }
        }
        return result
    }

    private fun isInactive(repo: Repo, today: LocalDate, now: Instant): Boolean {
        if (repo.firstSeenAt.isAfter(now.minus(Duration.ofDays(INACTIVE_DAYS)))) return false
        val before = snapshotRepository
            .findTopByRepoIdAndCapturedOnLessThanEqualOrderByCapturedOnDesc(repo.id, today.minusDays(INACTIVE_DAYS))
            ?: return false
        val latest = snapshotRepository.findTopByRepoIdOrderByCapturedOnDesc(repo.id) ?: return false
        return latest.stars <= before.stars
    }

    private fun fetchRepository(id: Long): GitHubRepo? {
        return try {
            gitHubClient.getRepository(id)
        } catch (e: GitHubRateLimitException) {
            if (!rateLimitWaiter.waitUntil(e.resetAt)) throw e
            gitHubClient.getRepository(id)
        }
    }

    private fun finish(run: CollectRun, result: CollectResult) {
        val clean = result.failed == 0 && result.discoverError == null && !result.stoppedByRateLimit
        run.status = if (clean) CollectStatus.SUCCEEDED else CollectStatus.PARTIAL
        run.finishedAt = Instant.now()
        run.discovered = result.discovered
        run.tracked = result.tracked
        run.failed = result.failed
        run.stoppedByRateLimit = result.stoppedByRateLimit
        run.error = result.discoverError?.let { shorten("discover: $it") }
        collectRunRepository.save(run)
    }

    private fun shorten(message: String) = message.take(1000)

    companion object {
        private const val DISCOVER_DAYS = 30L
        private const val DISCOVER_MIN_STARS = 50
        private const val INACTIVE_DAYS = 30L
    }
}
