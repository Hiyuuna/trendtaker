package io.github.hiyuuna.trendtaker

import io.github.hiyuuna.trendtaker.collect.CollectRun
import io.github.hiyuuna.trendtaker.collect.CollectRunRepository
import io.github.hiyuuna.trendtaker.collect.CollectStatus
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant

@WebMvcTest(StatusController::class)
class StatusControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockitoBean
    lateinit var collectRunRepository: CollectRunRepository

    @Test
    fun statusReturnsNameAndVersion() {
        mockMvc.get("/status").andExpect {
            status { isOk() }
            jsonPath("$.name") { value("trendtaker") }
            jsonPath("$.version") { value("0.1.0") }
        }
    }

    @Test
    fun lastCollectIsEmptyBeforeFirstRun() {
        given(collectRunRepository.findTopByOrderByStartedAtDesc()).willReturn(null)

        mockMvc.get("/status").andExpect {
            status { isOk() }
            jsonPath("$.lastCollect") { doesNotExist() }
        }
    }

    @Test
    fun statusShowsLastCollectWithoutErrorText() {
        given(collectRunRepository.findTopByOrderByStartedAtDesc()).willReturn(
            CollectRun(
                startedAt = Instant.parse("2026-11-28T00:05:00Z"),
                status = CollectStatus.PARTIAL,
                finishedAt = Instant.parse("2026-11-28T00:07:12Z"),
                discovered = 100,
                tracked = 98,
                failed = 2,
                error = "search down",
            ),
        )

        mockMvc.get("/status").andExpect {
            status { isOk() }
            jsonPath("$.lastCollect.status") { value("PARTIAL") }
            jsonPath("$.lastCollect.startedAt") { value("2026-11-28T00:05:00Z") }
            jsonPath("$.lastCollect.finishedAt") { value("2026-11-28T00:07:12Z") }
            jsonPath("$.lastCollect.discovered") { value(100) }
            jsonPath("$.lastCollect.tracked") { value(98) }
            jsonPath("$.lastCollect.failed") { value(2) }
            jsonPath("$.lastCollect.error") { doesNotExist() }
        }
    }
}
