package com.zeynepates.maisonparfait.backend.modules.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAddressRequest(
        @Size(max = 100) String title,
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 40) String phone,
        @NotBlank @Size(max = 100) String country,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 100) String district,
        @NotBlank @Size(max = 500) String addressLine,
        @Size(max = 20) String postalCode
) {}
