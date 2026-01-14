package com.zeynepates.maisonparfait.backend.modules.shipping;

public record ShippingAddressResponse(

        String fullName,
        String phone,
        String city,
        String district,
        String addressLine
) {
}
