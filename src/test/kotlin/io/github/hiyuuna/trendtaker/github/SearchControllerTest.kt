package io.github.hiyuuna.trendtaker.github

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant


@WebMvcTest(SearchController::class)
class SearchControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockitoBean
    lateinit var gitHubClient: GitHubClient

    @Test
    fun searchReturnsRepositoriesFromGitHub() {
        val repo = GitHubRepo(
            id = 1,
            fullName = "octocat/hello",
            description = null,
            language = "kotlin",
            stargazersCount = 42,
            forksCount = 3,
            openIssuesCount = 1,
            createdAt = Instant.parse("2026-11-01T00:00:00.00Z"),
            pushedAt = null,
            htmlUrl = "https://github.com/octocat/hello",
            fork = false,
            archived = false,
        )
        given(gitHubClient.searchRepositories("stars:>10000", 10)).willReturn(
                SearchResponse(
                    totalCount = 1,
                    items = listOf(repo)
                )
            )


        mockMvc.get("/search").andExpect {
                status { isOk() }
                jsonPath("$.total_count") { value(1) }
                jsonPath("$.items[0].full_name") { value("octocat/hello") }
                jsonPath("$.items[0].stargazers_count") { value(42) }
            }
    }


}