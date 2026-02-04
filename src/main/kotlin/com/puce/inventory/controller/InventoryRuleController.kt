package com.puce.inventory.controller

import com.puce.inventory.dto.InventoryRuleRequest
import com.puce.inventory.dto.InventoryRuleResponse
import com.puce.inventory.security.JwtUserExtractor
import com.puce.inventory.service.InventoryRuleService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Inventory Rules Controller
 *
 * Endpoints:
 * - GET /api/rules - List all rules (authenticated, any role)
 * - GET /api/rules/{id} - Get rule by id (authenticated, any role)
 * - GET /api/rules/active - List active rules (authenticated, any role)
 * - GET /api/rules/search?name=xxx - Search rules by name (authenticated, any role)
 * - POST /api/rules - Create a new rule (ADMIN only)
 * - PUT /api/rules/{id} - Update a rule (ADMIN only)
 * - PATCH /api/rules/{id}/toggle - Toggle active status (ADMIN only)
 * - DELETE /api/rules/{id} - Delete a rule (ADMIN only)
 */
@RestController
@RequestMapping("/api/rules")
class InventoryRuleController(
    private val service: InventoryRuleService,
    private val jwtUserExtractor: JwtUserExtractor
) {

    private val logger = LoggerFactory.getLogger(InventoryRuleController::class.java)

    // ==================== AUTHENTICATED ENDPOINTS (any role) ====================

    /**
     * Get all inventory rules
     * Requires: Valid JWT token (any role)
     */
    @GetMapping
    fun getAllRules(): ResponseEntity<List<InventoryRuleResponse>> {
        logger.debug("GET /api/rules - Fetching all rules")
        return ResponseEntity.ok(service.findAll())
    }

    /**
     * Get inventory rule by ID
     * Requires: Valid JWT token (any role)
     */
    @GetMapping("/{id}")
    fun getRuleById(@PathVariable id: Long): ResponseEntity<InventoryRuleResponse> {
        logger.debug("GET /api/rules/$id - Fetching rule by id")
        return ResponseEntity.ok(service.findById(id))
    }

    /**
     * Get all active inventory rules
     * Requires: Valid JWT token (any role)
     */
    @GetMapping("/active")
    fun getActiveRules(): ResponseEntity<List<InventoryRuleResponse>> {
        logger.debug("GET /api/rules/active - Fetching active rules")
        return ResponseEntity.ok(service.findActiveRules())
    }

    /**
     * Search inventory rules by name
     * Requires: Valid JWT token (any role)
     */
    @GetMapping("/search")
    fun searchRules(@RequestParam name: String): ResponseEntity<List<InventoryRuleResponse>> {
        logger.debug("GET /api/rules/search?name=$name - Searching rules")
        return ResponseEntity.ok(service.searchByName(name))
    }

    // ==================== ADMIN ONLY ENDPOINTS ====================

    /**
     * Create a new inventory rule
     * Requires: Valid JWT token with ADMIN role
     * Audits: Sets updatedBy from JWT 'sub' claim
     */
    @PostMapping
    fun createRule(
        @Valid @RequestBody request: InventoryRuleRequest
    ): ResponseEntity<InventoryRuleResponse> {
        val userId = jwtUserExtractor.extractUserId()
        logger.info("POST /api/rules - Creating rule by user: $userId")

        val created = service.create(request, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    /**
     * Update an existing inventory rule
     * Requires: Valid JWT token with ADMIN role
     * Audits: Sets updatedBy from JWT 'sub' claim
     */
    @PutMapping("/{id}")
    fun updateRule(
        @PathVariable id: Long,
        @Valid @RequestBody request: InventoryRuleRequest
    ): ResponseEntity<InventoryRuleResponse> {
        val userId = jwtUserExtractor.extractUserId()
        logger.info("PUT /api/rules/$id - Updating rule by user: $userId")

        val updated = service.update(id, request, userId)
        return ResponseEntity.ok(updated)
    }

    /**
     * Toggle the active status of an inventory rule
     * Requires: Valid JWT token with ADMIN role
     * Audits: Sets updatedBy from JWT 'sub' claim
     */
    @PatchMapping("/{id}/toggle")
    fun toggleRuleActive(@PathVariable id: Long): ResponseEntity<InventoryRuleResponse> {
        val userId = jwtUserExtractor.extractUserId()
        logger.info("PATCH /api/rules/$id/toggle - Toggling rule status by user: $userId")

        val toggled = service.toggleActive(id, userId)
        return ResponseEntity.ok(toggled)
    }

    /**
     * Delete an inventory rule
     * Requires: Valid JWT token with ADMIN role
     * Audits: Logs deletion intent with user ID
     */
    @DeleteMapping("/{id}")
    fun deleteRule(@PathVariable id: Long): ResponseEntity<Void> {
        val userId = jwtUserExtractor.extractUserId()
        logger.info("DELETE /api/rules/$id - Deleting rule by user: $userId")

        service.delete(id, userId)
        return ResponseEntity.noContent().build()
    }
}

