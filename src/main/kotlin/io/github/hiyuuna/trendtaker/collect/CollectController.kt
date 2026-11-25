package io.github.hiyuuna.trendtaker.collect

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CollectController(private val collectService: CollectService) {

    @PostMapping("/collect")
    fun collect(): CollectResult {
        return collectService.collect()
    }
}
