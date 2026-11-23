package io.github.hiyuuna.trendtaker.repo


import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "repositories")
class Repo(
    @Id
    val id: Long,
    var fullName: String,
    var description: String?,
    var language: String?,
    var htmlUrl: String,
    @JdbcTypeCode(SqlTypes.JSON)
    var topics: List<String>,
    var fork: Boolean,
    var archived: Boolean,
    val createdAt: Instant,
    val firstSeenAt: Instant,
    var updatedAt: Instant,
)