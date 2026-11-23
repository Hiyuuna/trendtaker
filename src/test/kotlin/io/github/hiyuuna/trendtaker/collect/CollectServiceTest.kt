package io.github.hiyuuna.trendtaker.collect

import io.github.hiyuuna.trendtaker.github.GitHubClient
import io.github.hiyuuna.trendtaker.github.GitHubRepo
import io.github.hiyuuna.trendtaker.github.SearchResponse
import io.github.hiyuuna.trendtaker.repo.RepoRepository
import io.github.hiyuuna.trendtaker.repo.RepoSnapshotRepository
import org.junit.jupiter.api.Test
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

    private fun repoWithStars(stars: Int) = GitHubRepo(
        id = 900001,
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

    @Test
    fun collectingTwiceOnSameDayKeepsOneSnapshot() {
        val morning = Instant.parse("2026-11-23T01:00:00Z")
        val evening = Instant.parse("2026-11-23T10:00:00Z")

        given(gitHubClient.searchRepositories("q", 100))
            .willReturn(SearchResponse(1, listOf(repoWithStars(100))))
        collectService.collect("q", morning)

        given(gitHubClient.searchRepositories("q", 100))
            .willReturn(SearchResponse(1, listOf(repoWithStars(120))))
        collectService.collect("q", evening)

        val snapshot = snapshotRepository.findByRepoIdAndCapturedOn(900001, LocalDate.parse("2026-11-23"))
        assertTrue(repoRepository.existsById(900001))
        assertEquals(120, snapshot?.stars)
    }
}
