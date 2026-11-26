package io.github.hiyuuna.trendtaker.github

import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming
import java.time.Instant


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SearchResponse(
    val totalCount: Int,
    val items: List<GitHubRepo>,
)


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GitHubRepo(
    val id: Long,
    val fullName: String,
    val description: String?,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val openIssuesCount: Int,
    val createdAt: Instant,
    val pushedAt: Instant?,
    val htmlUrl: String,
    val topics: List<String> = emptyList(),
    val fork: Boolean,
    val archived: Boolean,

    )




