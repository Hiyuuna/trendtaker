package io.github.hiyuuna.trendtaker.collect

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty(name = ["collect.manual-enabled"], havingValue = "true")
class CollectController(private val collectService: CollectService) {

    @PostMapping("/collect")
    fun collect(): CollectResult {
        return collectService.collect()
    }
}
