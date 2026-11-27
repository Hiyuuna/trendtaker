package io.github.hiyuuna.trendtaker.trend

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate

@WebMvcTest(TrendController::class)
class TrendControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockitoBean
    lateinit var trendService: TrendService

    private val response = TrendResponse(
        from = LocalDate.parse("2026-11-28"),
        to = LocalDate.parse("2026-11-29"),
        items = listOf(TrendItem(1, "octocat/hello", "https://github.com/octocat/hello", 1840, 312)),
    )

    @Test
    fun trendingReturnsRankedItems() {
        given(trendService.trending(20)).willReturn(response)

        mockMvc.get("/trending").andExpect {
            status { isOk() }
            jsonPath("$.from") { value("2026-11-28") }
            jsonPath("$.to") { value("2026-11-29") }
            jsonPath("$.items[0].rank") { value(1) }
            jsonPath("$.items[0].fullName") { value("octocat/hello") }
            jsonPath("$.items[0].gained") { value(312) }
        }
    }

    @Test
    fun limitParameterIsPassedToService() {
        given(trendService.trending(5)).willReturn(response)

        mockMvc.get("/trending?limit=5").andExpect {
            status { isOk() }
            jsonPath("$.items[0].stars") { value(1840) }
        }
    }
}
