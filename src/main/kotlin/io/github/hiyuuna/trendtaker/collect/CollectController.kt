package io.github.hiyuuna.trendtaker.collect

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty(name = ["collect.manual-enabled"], havingValue = "true")
class CollectController(private val collectService: CollectService) {

    @PostMapping("/collect")
    fun collect(): ResponseEntity<CollectResult> {
        val result = collectService.collect() ?: return ResponseEntity.status(HttpStatus.CONFLICT).build()
        return ResponseEntity.ok(result)
    }
}
