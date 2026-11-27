package io.github.hiyuuna.trendtaker.repo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface RepoSnapshotRepository : JpaRepository<RepoSnapshot, Long> {
    fun findByRepoIdAndCapturedOn(repoId: Long, capturedOn: LocalDate): RepoSnapshot?

    fun findTopByRepoIdOrderByCapturedOnDesc(repoId: Long): RepoSnapshot?

    fun findTopByRepoIdAndCapturedOnLessThanEqualOrderByCapturedOnDesc(repoId: Long, capturedOn: LocalDate): RepoSnapshot?

    fun findByCapturedOn(capturedOn: LocalDate): List<RepoSnapshot>

    @Query("select max(s.capturedOn) from RepoSnapshot s")
    fun findLatestCapturedOn(): LocalDate?

    @Query("select max(s.capturedOn) from RepoSnapshot s where s.capturedOn < :day")
    fun findLatestCapturedOnBefore(@Param("day") day: LocalDate): LocalDate?
}
