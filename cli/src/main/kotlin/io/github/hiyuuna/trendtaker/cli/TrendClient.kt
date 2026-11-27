package io.github.hiyuuna.trendtaker.cli

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TrendResponse(
    val from: String? = null,
    val to: String? = null,
    val items: List<TrendItem> = emptyList(),
)

@Serializable
data class TrendItem(
    val rank: Int,
    val fullName: String,
    val htmlUrl: String,
    val stars: Int,
    val gained: Int,
)

class TrendClient(private val server: String) {

    suspend fun trending(limit: Int): TrendResponse {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        try {
            return client.get("${server.trimEnd('/')}/trending") {
                parameter("limit", limit)
            }.body()
        } finally {
            client.close()
        }
    }
}
