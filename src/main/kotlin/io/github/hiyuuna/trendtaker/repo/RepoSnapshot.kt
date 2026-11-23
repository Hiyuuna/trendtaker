package io.github.hiyuuna.trendtaker.repo

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "repo_snapshots")
class RepoSnapshot(
    val repoId: Long,
    val capturedOn: LocalDate,
    var stars: Int,
    var forks: Int,
    var openIssues: Int,
    var capturedAt: Instant,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)