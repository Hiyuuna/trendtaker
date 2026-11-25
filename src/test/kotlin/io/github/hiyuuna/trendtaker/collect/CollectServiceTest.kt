package io.github.hiyuuna.trendtaker.collect

import io.github.hiyuuna.trendtaker.github.GitHubClient
import io.github.hiyuuna.trendtaker.github.GitHubRateLimitException
import io.github.hiyuuna.trendtaker.github.GitHubRepo
import io.github.hiyuuna.trendtaker.github.SearchResponse
import io.github.hiyuuna.trendtaker.repo.RepoRepository
import io.github.hiyuuna.trendtaker.repo.RepoSnapshotRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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

    @Autowired
    lateinit var collectRunRepository: CollectRunRepository

    @MockitoBean
    lateinit var gitHubClient: GitHubClient

    @MockitoBean
    lateinit var rateLimitWaiter: RateLimitWaiter

    private val repoId = 900001L
    private val otherRepoId = 900002L
    private val day1 = Instant.parse("2026-11-23T01:00:00Z")
    private val day2 = Instant.parse("2026-11-24T01:00:00Z")

    private fun repo(id: Long = repoId, stars: Int = 100) = GitHubRepo(
        id = id,
        fullName = "octocat/hello-$id",
        description = null,
        language = "Kotlin",
        stargazersCount = stars,
        forksCount = 5,
        openIssuesCount = 2,
        createdAt = Instant.parse("2020-01-01T00:00:00Z"),
        pushedAt = null,
        htmlUrl = "https://github.com/octocat/hello-$id",
        topics = listOf("kotlin"),
        fork = false,
        archived = false,
    )

    private fun searchReturns(vararg repos: GitHubRepo) {
        given(gitHubClient.searchRepositories(anyString(), anyInt()))
            .willReturn(SearchResponse(repos.size, repos.toList()))
    }

    private fun starsOn(id: Long, day: String) =
        snapshotRepository.findByRepoIdAndCapturedOn(id, LocalDate.parse(day))?.stars

    @Test
    fun collectingTwiceOnSameDayKeepsOneSnapshot() {
        searchReturns(repo(stars = 100))
        given(gitHubClient.getRepository(repoId)).willReturn(repo(stars = 100))
        collectService.collect(day1)

        given(gitHubClient.getRepository(repoId)).willReturn(repo(stars = 120))
        val second = collectService.collect(Instant.parse("2026-11-23T10:00:00Z"))

        assertTrue(repoRepository.existsById(repoId))
        assertEquals(100, starsOn(repoId, "2026-11-23"))
        assertEquals(0, second?.tracked)
    }

    @Test
    fun repoMissingFromSearchIsStillTracked() {
        searchReturns(repo(stars = 100))
        given(gitHubClient.getRepository(repoId)).willReturn(repo(stars = 100))
        collectService.collect(day1)

        searchReturns()
        given(gitHubClient.getRepository(repoId)).willReturn(repo(stars = 150))
        collectService.collect(day2)

        assertEquals(150, starsOn(repoId, "2026-11-24"))
    }

    @Test
    fun discoverFailureDoesNotStopTracking() {
        searchReturns(repo(stars = 100))
        given(gitHubClient.getRepository(repoId)).willReturn(repo(stars = 100))
        collectService.collect(day1)

        given(gitHubClient.searchRepositories(anyString(), anyInt())).willThrow(RuntimeException("search down"))
        given(gitHubClient.getRepository(repoId)).willReturn(repo(stars = 130))
        val result = collectService.collect(day2)

        assertNotNull(result?.discoverError)
        assertEquals(1, result?.tracked)
        assertEquals(130, starsOn(repoId, "2026-11-24"))
    }

    @Test
    fun oneRepoFailureDoesNotStopOthers() {
        searchReturns(repo(repoId), repo(otherRepoId))
        given(gitHubClient.getRepository(repoId)).willThrow(RuntimeException("server error"))
        given(gitHubClient.getRepository(otherRepoId)).willReturn(repo(otherRepoId, stars = 70))
        val result = collectService.collect(day1)

        assertEquals(1, result?.failed)
        assertEquals(1, result?.tracked)
        assertEquals(70, starsOn(otherRepoId, "2026-11-23"))
    }

    @Test
    fun rateLimitWaitsAndRetriesWhenResetIsSoon() {
        searchReturns(repo(stars = 100))
        given(gitHubClient.getRepository(repoId))
            .willThrow(GitHubRateLimitException(day1))
            .willReturn(repo(stars = 100))
        given(rateLimitWaiter.waitUntil(any(Instant::class.java) ?: day1)).willReturn(true)
        val result = collectService.collect(day1)

        assertEquals(1, result?.tracked)
        assertEquals(100, starsOn(repoId, "2026-11-23"))
    }

    @Test
    fun rateLimitStopsTrackingWhenResetIsFar() {
        searchReturns(repo(repoId), repo(otherRepoId))
        given(gitHubClient.getRepository(repoId)).willThrow(GitHubRateLimitException(day2))
        given(gitHubClient.getRepository(otherRepoId)).willThrow(GitHubRateLimitException(day2))
        given(rateLimitWaiter.waitUntil(any(Instant::class.java) ?: day1)).willReturn(false)
        val result = collectService.collect(day1)

        assertTrue(result?.stoppedByRateLimit == true)
        assertEquals(0, result?.tracked)
        assertNull(starsOn(repoId, "2026-11-23"))
    }

    @Test
    fun successfulCollectIsRecorded() {
        searchReturns(repo(stars = 100))
        given(gitHubClient.getRepository(repoId)).willReturn(repo(stars = 100))
        collectService.collect(day1)

        val run = collectRunRepository.findAll().single()
        assertEquals(CollectStatus.SUCCEEDED, run.status)
        assertEquals(day1, run.startedAt)
        assertNotNull(run.finishedAt)
        assertEquals(1, run.discovered)
        assertEquals(1, run.tracked)
        assertEquals(0, run.failed)
    }

    @Test
    fun discoverFailureIsRecordedAsPartial() {
        given(gitHubClient.searchRepositories(anyString(), anyInt())).willThrow(RuntimeException("search down"))
        collectService.collect(day1)

        val run = collectRunRepository.findAll().single()
        assertEquals(CollectStatus.PARTIAL, run.status)
        assertTrue(run.error!!.contains("search down"))
    }
}
