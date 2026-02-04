package com.puce.inventory.repository

import com.puce.inventory.entity.InventoryRule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InventoryRuleRepository : JpaRepository<InventoryRule, Long> {

    fun findByIsActiveTrue(): List<InventoryRule>

    fun findByIsActiveFalse(): List<InventoryRule>

    fun findByNameContainingIgnoreCase(name: String): List<InventoryRule>

    fun findByNameIgnoreCase(name: String): InventoryRule?

    fun existsByNameIgnoreCase(name: String): Boolean
}

