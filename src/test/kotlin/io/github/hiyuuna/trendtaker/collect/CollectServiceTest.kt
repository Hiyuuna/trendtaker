package io.github.hiyuuna.trendtaker.collect

import io.github.hiyuuna.trendtaker.github.GitHubClient
import io.github.hiyuuna.trendtaker.github.GitHubRepo
import io.github.hiyuuna.trendtaker.github.SearchResponse
import io.github.hiyuuna.trendtaker.repo.RepoRepository
import io.github.hiyuuna.trendtaker.repo.RepoSnapshotRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class CollectServiceTest {

    @Autowired
    lateinit var collectService: CollectService

    @Autowired
    lateinit var repoRepository: RepoRepository

    @Autowired
    lateinit var snapshotRepository: RepoSnapshotRepository

    @MockitoBean
    lateinit var gitHubClient: GitHubClient

    private val repoId = 900001L

    private fun repoWithStars(stars: Int) = GitHubRepo(
        id = repoId,
        fullName = "octocat/hello-world",
        description = null,
        language = "Kotlin",
        stargazersCount = stars,
        forksCount = 5,
        openIssuesCount = 2,
        createdAt = Instant.parse("2020-01-01T00:00:00Z"),
        pushedAt = null,
        htmlUrl = "https://github.com/octocat/hello-world",
        topics = listOf("kotlin"),
        fork = false,
        archived = false,
    )

    private fun searchReturns(vararg repos: GitHubRepo) {
        given(gitHubClient.searchRepositories(anyString(), anyInt()))
            .willReturn(SearchResponse(repos.size, repos.toList()))
    }

    @Test
    fun collectingTwiceOnSameDayKeepsOneSnapshot() {
        searchReturns(repoWithStars(100))
        given(gitHubClient.getRepository(repoId)).willReturn(repoWithStars(100))
        collectService.collect(Instant.parse("2026-11-23T01:00:00Z"))

        given(gitHubClient.getRepository(repoId)).willReturn(repoWithStars(120))
        val second = collectService.collect(Instant.parse("2026-11-23T10:00:00Z"))

        val snapshot = snapshotRepository.findByRepoIdAndCapturedOn(repoId, LocalDate.parse("2026-11-23"))
        assertTrue(repoRepository.existsById(repoId))
        assertEquals(100, snapshot?.stars)
        assertEquals(0, second.tracked)
    }

    @Test
    fun repoMissingFromSearchIsStillTracked() {
        searchReturns(repoWithStars(100))
        given(gitHubClient.getRepository(repoId)).willReturn(repoWithStars(100))
        collectService.collect(Instant.parse("2026-11-23T01:00:00Z"))

        searchReturns()
        given(gitHubClient.getRepository(repoId)).willReturn(repoWithStars(150))
        collectService.collect(Instant.parse("2026-11-24T01:00:00Z"))

        val nextDay = snapshotRepository.findByRepoIdAndCapturedOn(repoId, LocalDate.parse("2026-11-24"))
        assertEquals(150, nextDay?.stars)
    }
}
