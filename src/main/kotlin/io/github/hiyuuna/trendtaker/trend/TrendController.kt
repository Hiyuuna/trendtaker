package io.github.hiyuuna.trendtaker.trend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TrendController(private val trendService: TrendService) {

    @GetMapping("/trending")
    fun trending(@RequestParam(defaultValue = "20") limit: Int): TrendResponse =
        trendService.trending(limit)
}
