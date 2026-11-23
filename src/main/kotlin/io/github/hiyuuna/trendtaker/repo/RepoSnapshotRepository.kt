package io.github.hiyuuna.trendtaker.repo

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface RepoSnapshotRepository : JpaRepository<RepoSnapshot, Long> {
    fun findByRepoIdAndCapturedOn(repoId: Long, capturedOn: LocalDate): RepoSnapshot?
}