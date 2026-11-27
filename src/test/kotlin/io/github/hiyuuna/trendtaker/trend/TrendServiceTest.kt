package io.github.hiyuuna.trendtaker.trend

import io.github.hiyuuna.trendtaker.repo.Repo
import io.github.hiyuuna.trendtaker.repo.RepoRepository
import io.github.hiyuuna.trendtaker.repo.RepoSnapshot
import io.github.hiyuuna.trendtaker.repo.RepoSnapshotRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class TrendServiceTest {

    @Autowired
    lateinit var trendService: TrendService

    @Autowired
    lateinit var repoRepository: RepoRepository

    @Autowired
    lateinit var snapshotRepository: RepoSnapshotRepository

    private val now = Instant.parse("2099-01-02T01:00:00Z")
    private val day1 = "2099-01-01"
    private val day2 = "2099-01-02"
    private val first = 910001L
    private val second = 910002L
    private val third = 910003L

    private fun saveRepo(id: Long) {
        repoRepository.save(
            Repo(
                id = id,
                fullName = "octocat/hello-$id",
                description = null,
                language = "Kotlin",
                htmlUrl = "https://github.com/octocat/hello-$id",
                topics = emptyList(),
                fork = false,
                archived = false,
                createdAt = now,
                firstSeenAt = now,
                updatedAt = now,
            ),
        )
    }

    private fun saveSnapshot(id: Long, day: String, stars: Int) {
        snapshotRepository.save(
            RepoSnapshot(
                repoId = id,
                capturedOn = LocalDate.parse(day),
                stars = stars,
                forks = 0,
                openIssues = 0,
                capturedAt = now,
            ),
        )
    }

    @Test
    fun reposAreRankedByStarsGained() {
        listOf(first, second, third).forEach { saveRepo(it) }
        saveSnapshot(first, day1, 100)
        saveSnapshot(second, day1, 100)
        saveSnapshot(third, day1, 100)
        saveSnapshot(first, day2, 150)
        saveSnapshot(second, day2, 300)
        saveSnapshot(third, day2, 100)

        val result = trendService.trending(20)

        assertEquals(LocalDate.parse(day1), result.from)
        assertEquals(LocalDate.parse(day2), result.to)
        assertEquals(listOf("octocat/hello-$second", "octocat/hello-$first"), result.items.map { it.fullName })
        assertEquals(listOf(1, 2), result.items.map { it.rank })
        assertEquals(listOf(200, 50), result.items.map { it.gained })
        assertEquals(300, result.items.first().stars)
    }

    @Test
    fun repoWithoutPreviousSnapshotIsExcluded() {
        listOf(first, second).forEach { saveRepo(it) }
        saveSnapshot(first, day1, 100)
        saveSnapshot(first, day2, 130)
        saveSnapshot(second, day2, 5000)

        val result = trendService.trending(20)

        assertEquals(listOf("octocat/hello-$first"), result.items.map { it.fullName })
    }

    @Test
    fun limitCutsTheList() {
        listOf(first, second, third).forEach { saveRepo(it) }
        listOf(first, second, third).forEach { saveSnapshot(it, day1, 100) }
        saveSnapshot(first, day2, 110)
        saveSnapshot(second, day2, 120)
        saveSnapshot(third, day2, 130)

        val result = trendService.trending(2)

        assertEquals(listOf(30, 20), result.items.map { it.gained })
    }

    @Test
    fun latestTwoCollectedDaysAreComparedEvenWithGap() {
        saveRepo(first)
        saveSnapshot(first, day1, 100)
        saveSnapshot(first, "2099-01-05", 180)

        val result = trendService.trending(20)

        assertEquals(LocalDate.parse(day1), result.from)
        assertEquals(LocalDate.parse("2099-01-05"), result.to)
        assertEquals(80, result.items.single().gained)
    }

    @Test
    fun noItemsWhenLatestDayHasNothingToCompare() {
        saveRepo(first)
        saveSnapshot(first, day2, 100)

        val result = trendService.trending(20)

        assertEquals(LocalDate.parse(day2), result.to)
        assertTrue(result.items.isEmpty())
    }
}
