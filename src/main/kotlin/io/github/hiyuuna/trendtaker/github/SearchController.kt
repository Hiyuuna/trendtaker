package io.github.hiyuuna.trendtaker.github

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class SearchController(private val gitHubClient: GitHubClient) {

    @GetMapping("/search")
    fun search(
        @RequestParam(defaultValue = "stars:>10000") q: String,
    ): SearchResponse = gitHubClient.searchRepositories(q)
}
