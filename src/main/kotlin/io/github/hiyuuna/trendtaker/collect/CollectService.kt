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

@Service
class CollectService(
    private val gitHubClient: GitHubClient,
    private val repoRepository: RepoRepository,
    private val snapshotRepository: RepoSnapshotRepository,
) {
    fun collect(query: String, now: Instant = Instant.now()): Int {
        val today = LocalDate.ofInstant(now, ZoneOffset.UTC)
        val items = gitHubClient.searchRepositories(query, 100).items
        for (item in items) {
            saveRepo(item, now)
            saveSnapshot(item, today, now)
        }
        return items.size
    }

    private fun saveRepo(item: GitHubRepo, now: Instant) {
        val htmlUrl = item.htmlUrl ?: "https://github.com/${item.fullName}"
        val repo = repoRepository.findById(item.id).orElse(null)
        if (repo == null) {
            repoRepository.save(
                Repo(
                    id = item.id,
                    fullName = item.fullName,
                    description = item.description,
                    language = item.language,
                    htmlUrl = htmlUrl,
                    topics = item.topics,
                    fork = item.fork,
                    archived = item.archived,
                    createdAt = item.createdAt,
                    firstSeenAt = now,
                    updatedAt = now,
                )
            )
        } else {
            repo.fullName = item.fullName
            repo.description = item.description
            repo.language = item.language
            repo.htmlUrl = htmlUrl
            repo.topics = item.topics
            repo.fork = item.fork
            repo.archived = item.archived
            repo.updatedAt = now
            repoRepository.save(repo)
        }
    }

    private fun saveSnapshot(item: GitHubRepo, today: LocalDate, now: Instant) {
        val snapshot = snapshotRepository.findByRepoIdAndCapturedOn(item.id, today)
        if (snapshot == null) {
            snapshotRepository.save(
                RepoSnapshot(
                    repoId = item.id,
                    capturedOn = today,
                    stars = item.stargazersCount,
                    forks = item.forksCount,
                    openIssues = item.openIssuesCount,
                    capturedAt = now,
                )
            )
        } else {
            snapshot.stars = item.stargazersCount
            snapshot.forks = item.forksCount
            snapshot.openIssues = item.openIssuesCount
            snapshot.capturedAt = now
            snapshotRepository.save(snapshot)
        }
    }
}
