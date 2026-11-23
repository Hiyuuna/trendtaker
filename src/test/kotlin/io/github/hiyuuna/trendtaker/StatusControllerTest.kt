package io.github.hiyuuna.trendtaker

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


@WebMvcTest(StatusController::class)
class StatusControllerTest(@Autowired val mockMvc: MockMvc) {

    @Test
    fun statusReturnsNameAndVersion() {
        mockMvc.get("/status").andExpect {
                status { isOk() }
                jsonPath("$.name") { value("trendtaker") }
                jsonPath("$.version") { value("0.1.0") }
            }
    }

}