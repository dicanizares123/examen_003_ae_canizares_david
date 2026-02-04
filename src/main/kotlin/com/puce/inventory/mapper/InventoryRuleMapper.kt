package com.puce.inventory.mapper

import com.puce.inventory.dto.InventoryRuleRequest
import com.puce.inventory.dto.InventoryRuleResponse
import com.puce.inventory.entity.InventoryRule
import org.springframework.stereotype.Component

@Component
class InventoryRuleMapper {

    fun toEntity(request: InventoryRuleRequest, updatedBy: String): InventoryRule {
        return InventoryRule(
            name = request.name,
            description = request.description,
            isActive = request.isActive,
            updatedBy = updatedBy
        )
    }

    fun toResponse(entity: InventoryRule): InventoryRuleResponse {
        return InventoryRuleResponse(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            isActive = entity.isActive,
            updatedBy = entity.updatedBy,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toResponseList(entities: List<InventoryRule>): List<InventoryRuleResponse> {
        return entities.map { toResponse(it) }
    }

    fun updateEntity(entity: InventoryRule, request: InventoryRuleRequest, updatedBy: String): InventoryRule {
        entity.apply {
            name = request.name
            description = request.description
            isActive = request.isActive
            this.updatedBy = updatedBy
        }
        return entity
    }
}
