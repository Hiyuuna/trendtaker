package io.github.hiyuuna.trendtaker.collect

import io.github.hiyuuna.trendtaker.github.GitHubClient
import io.github.hiyuuna.trendtaker.github.GitHubRepo
import io.github.hiyuuna.trendtaker.repo.Repo
import io.github.hiyuuna.trendtaker.repo.RepoRepository
import io.github.hiyuuna.trendtaker.repo.RepoSnapshot
import io.github.hiyuuna.trendtaker.repo.RepoSnapshotRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class CollectResult(val discovered: Int, val tracked: Int)

@Service
class CollectService(
    private val gitHubClient: GitHubClient,
    private val repoRepository: RepoRepository,
    private val snapshotRepository: RepoSnapshotRepository,
) {
    fun collect(now: Instant = Instant.now()): CollectResult {
        val today = LocalDate.ofInstant(now, ZoneOffset.UTC)
        val discovered = discover(today, now)
        val tracked = track(today, now)
        return CollectResult(discovered, tracked)
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
                    htmlUrl = htmlUrlOf(item),
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

    private fun track(today: LocalDate, now: Instant): Int {
        var tracked = 0
        for (repo in repoRepository.findAll()) {
            if (snapshotRepository.findByRepoIdAndCapturedOn(repo.id, today) != null) continue
            val item = gitHubClient.getRepository(repo.id) ?: continue

            repo.fullName = item.fullName
            repo.description = item.description
            repo.language = item.language
            repo.htmlUrl = htmlUrlOf(item)
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
            tracked++
        }
        return tracked
    }

    private fun htmlUrlOf(item: GitHubRepo): String {
        return item.htmlUrl ?: "https://github.com/${item.fullName}"
    }

    companion object {
        private const val DISCOVER_DAYS = 30L
        private const val DISCOVER_MIN_STARS = 50
    }
}
