package com.puce.inventory.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class InventoryRuleRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String,

    @field:Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String? = null,

    val isActive: Boolean = true
)

