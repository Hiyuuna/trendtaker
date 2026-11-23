package io.github.hiyuuna.trendtaker.collect

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CollectController(private val collectService: CollectService) {

    @PostMapping("/collect")
    fun collect(@RequestParam(defaultValue = "stars:>10000") q: String): Map<String, Int> {
        return mapOf("collected" to collectService.collect(q))
    }
}
