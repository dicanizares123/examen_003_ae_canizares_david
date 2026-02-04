package com.puce.inventory

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
	"spring.datasource.url=jdbc:postgresql://localhost:5432/inventory_db",
	"spring.security.oauth2.resourceserver.jwt.issuer-uri=https://example.com",
	"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com/.well-known/jwks.json"
])
class InventoryApplicationTests {

	@Test
	fun contextLoads() {
	}

}
