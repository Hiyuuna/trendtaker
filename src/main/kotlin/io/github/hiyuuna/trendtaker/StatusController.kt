package io.github.hiyuuna.trendtaker

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant


data class StatusResponse(
    val name: String,
    val version: String,
    val time: Instant,
)

@RestController
class StatusController {

    @GetMapping("/status")
    fun status(): StatusResponse =
        StatusResponse(
            name = "trendtaker",
            version = "0.1.0",
            time = Instant.now(),
        )


}