package io.github.hiyuuna.trendtaker.repo


import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.test.assertEquals

@SpringBootTest
@Transactional
class RepoRepositoryTest {

    @Autowired
    lateinit var repoRepository: RepoRepository

    @Test
    fun `저장한 리포를 다시 읽을 수 있다`() {
        val now = Instant.now()
        val repo = Repo(
            id = 1,
            fullName = "octocat/hello-world",
            description = null,
            language = "Kotlin",
            htmlUrl = "https://github.com/octocat/hello-world",
            topics = listOf("kotlin", "spring"),
            fork = false,
            archived = false,
            createdAt = now,
            firstSeenAt = now,
            updatedAt = now,
        )

        repoRepository.saveAndFlush(repo)
        val found = repoRepository.findById(1).orElseThrow()

        assertEquals("octocat/hello-world", found.fullName)
        assertEquals(listOf("kotlin", "spring"), found.topics)
    }
}