package com.zeynepates.maisonparfait.backend.common.security;

/**
 * Identifies the caller for a given request. Populated as the Authentication
 * principal by whichever module owns authentication (the identity module,
 * once it exists) - common only defines the shape.
 */
public record CurrentUser(Long id, String role) {
}
