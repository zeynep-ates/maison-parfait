package com.zeynepates.maisonparfait.backend.modules.shipping;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ShippingAddress {
    private Long id;
    private String fullName;
    private String phone;
    private String city;
    private String district;
    private String addressLine;
}
