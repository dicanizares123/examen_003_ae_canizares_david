package com.puce.inventory.dto

import java.time.LocalDateTime

data class HealthResponse(
    val status: String = "OK",
    val service: String = "inventory-config-service",
    val timestamp: LocalDateTime = LocalDateTime.now()
)

