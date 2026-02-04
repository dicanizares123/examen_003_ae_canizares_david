package com.puce.inventory.service

import com.puce.inventory.dto.InventoryRuleRequest
import com.puce.inventory.dto.InventoryRuleResponse
import com.puce.inventory.entity.InventoryRule
import com.puce.inventory.exception.BadRequestException
import com.puce.inventory.exception.NotFoundException
import com.puce.inventory.mapper.InventoryRuleMapper
import com.puce.inventory.repository.InventoryRuleRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class InventoryRuleService(
    private val repository: InventoryRuleRepository,
    private val mapper: InventoryRuleMapper
) {

    private val logger = LoggerFactory.getLogger(InventoryRuleService::class.java)

    @Transactional(readOnly = true)
    fun findAll(): List<InventoryRuleResponse> {
        logger.debug("Fetching all inventory rules")
        return mapper.toResponseList(repository.findAll())
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): InventoryRuleResponse {
        logger.debug("Fetching inventory rule with id: $id")
        val rule = findEntityById(id)
        return mapper.toResponse(rule)
    }

    @Transactional(readOnly = true)
    fun findActiveRules(): List<InventoryRuleResponse> {
        logger.debug("Fetching active inventory rules")
        return mapper.toResponseList(repository.findByIsActiveTrue())
    }

    @Transactional(readOnly = true)
    fun searchByName(name: String): List<InventoryRuleResponse> {
        logger.debug("Searching inventory rules by name: $name")
        return mapper.toResponseList(repository.findByNameContainingIgnoreCase(name))
    }

    fun create(request: InventoryRuleRequest, userId: String): InventoryRuleResponse {
        logger.info("Creating inventory rule with name: ${request.name} by user: $userId")

        if (repository.existsByNameIgnoreCase(request.name)) {
            throw BadRequestException("A rule with name '${request.name}' already exists")
        }

        val entity = mapper.toEntity(request, userId)
        val saved = repository.save(entity)

        logger.info("Created inventory rule with id: ${saved.id} by user: $userId")
        return mapper.toResponse(saved)
    }

    fun update(id: Long, request: InventoryRuleRequest, userId: String): InventoryRuleResponse {
        logger.info("Updating inventory rule with id: $id by user: $userId")

        val existingRule = findEntityById(id)

        // Check if name is being changed to a name that already exists
        val ruleWithSameName = repository.findByNameIgnoreCase(request.name)
        if (ruleWithSameName != null && ruleWithSameName.id != id) {
            throw BadRequestException("A rule with name '${request.name}' already exists")
        }

        val updated = mapper.updateEntity(existingRule, request, userId)
        val saved = repository.save(updated)

        logger.info("Updated inventory rule with id: ${saved.id} by user: $userId")
        return mapper.toResponse(saved)
    }

    fun toggleActive(id: Long, userId: String): InventoryRuleResponse {
        logger.info("Toggling active status for inventory rule with id: $id by user: $userId")

        val rule = findEntityById(id)
        rule.isActive = !rule.isActive
        rule.updatedBy = userId

        val saved = repository.save(rule)

        logger.info("Toggled inventory rule with id: ${saved.id} to isActive: ${saved.isActive} by user: $userId")
        return mapper.toResponse(saved)
    }

    fun delete(id: Long, userId: String) {
        logger.info("Deleting inventory rule with id: $id by user: $userId")

        val rule = findEntityById(id)

        // Log deletion intent for audit
        logger.warn("AUDIT: User $userId deleted inventory rule - id: ${rule.id}, name: ${rule.name}")

        repository.delete(rule)

        logger.info("Deleted inventory rule with id: $id by user: $userId")
    }

    private fun findEntityById(id: Long): InventoryRule {
        return repository.findById(id)
            .orElseThrow { NotFoundException("Inventory rule with id $id not found") }
    }
}

