package com.puce.inventory.controller

import com.puce.inventory.dto.HealthResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Public controller - No authentication required
 */
@RestController
@RequestMapping("/public")
class PublicController {

    /**
     * Health check endpoint - accessible without authentication
     * GET /public/health
     */
    @GetMapping("/health")
    fun health(): ResponseEntity<HealthResponse> {
        return ResponseEntity.ok(HealthResponse())
    }
}
