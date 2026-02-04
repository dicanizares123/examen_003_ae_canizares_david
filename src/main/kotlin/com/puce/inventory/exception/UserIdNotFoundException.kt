package com.puce.inventory.exception

class UserIdNotFoundException(message: String = "User ID (sub claim) not found in JWT token") : RuntimeException(message)

