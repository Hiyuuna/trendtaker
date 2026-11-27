package io.github.hiyuuna.trendtaker.trend

import io.github.hiyuuna.trendtaker.repo.RepoRepository
import io.github.hiyuuna.trendtaker.repo.RepoSnapshotRepository
import org.springframework.stereotype.Service

@Service
class TrendService(
    private val snapshotRepository: RepoSnapshotRepository,
    private val repoRepository: RepoRepository,
) {

    fun trending(limit: Int): TrendResponse {
        val to = snapshotRepository.findLatestCapturedOn()
            ?: return TrendResponse(null, null, emptyList())
        val from = snapshotRepository.findLatestCapturedOnBefore(to)
            ?: return TrendResponse(null, to, emptyList())

        val starsBefore = snapshotRepository.findByCapturedOn(from).associate { it.repoId to it.stars }
        val gains = snapshotRepository.findByCapturedOn(to)
            .mapNotNull { now -> starsBefore[now.repoId]?.let { Gain(now.repoId, now.stars, now.stars - it) } }
            .filter { it.gained > 0 }
            .sortedWith(compareByDescending<Gain> { it.gained }.thenByDescending { it.stars }.thenBy { it.repoId })
            .take(limit.coerceIn(1, MAX_LIMIT))

        val repos = repoRepository.findAllById(gains.map { it.repoId }).associateBy { it.id }
        val items = gains.mapIndexed { index, gain ->
            val repo = repos.getValue(gain.repoId)
            TrendItem(index + 1, repo.fullName, repo.htmlUrl, gain.stars, gain.gained)
        }
        return TrendResponse(from, to, items)
    }

    private data class Gain(val repoId: Long, val stars: Int, val gained: Int)

    companion object {
        const val MAX_LIMIT = 100
    }
}
