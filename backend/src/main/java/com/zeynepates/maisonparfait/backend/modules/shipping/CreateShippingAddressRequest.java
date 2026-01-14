package com.zeynepates.maisonparfait.backend.modules.shipping;

import jakarta.validation.constraints.NotBlank;

public record CreateShippingAddressRequest(
        @NotBlank String fullName,
        @NotBlank String phone,
        @NotBlank String city,
        @NotBlank String district,
        @NotBlank String addressLine
) {
}
