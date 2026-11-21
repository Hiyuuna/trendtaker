package io.github.hiyuuna.trendtaker.github

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class GitHubClient {
    private val restClient = RestClient.builder()
        .baseUrl("https://api.github.com")
        .defaultHeader("Accept", "application/vnd.github+json")
        .defaultHeader("User-Agent", "trendtaker")
        .build()

    fun searchRepositories(query: String, perPage: Int = 10): SearchResponse {
        return restClient.get()
            .uri("/search/repositories?q={q}&sort=stars&order=desc&per_page={perPage}", query, perPage)
            .retrieve()
            .body(SearchResponse::class.java)
            ?: throw IllegalArgumentException("GitHub response body is empty")
    }

}