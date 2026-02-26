package com.zeynepates.maisonparfait.backend.modules.address;

public record AddressResponse(
        Long id,
        String title,
        String fullName,
        String phone,
        String country,
        String city,
        String district,
        String addressLine,
        String postalCode
) {}
