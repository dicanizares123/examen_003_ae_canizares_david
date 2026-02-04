package com.puce.inventory.dto

import java.time.LocalDateTime

data class InventoryRuleResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val updatedBy: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

