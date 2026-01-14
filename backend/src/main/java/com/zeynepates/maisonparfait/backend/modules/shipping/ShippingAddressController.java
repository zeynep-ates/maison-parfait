package com.zeynepates.maisonparfait.backend.modules.shipping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shipping-addresses")
public class ShippingAddressController {

    private final ShippingAddressService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@Valid @RequestBody CreateShippingAddressRequest req) {
        return service.create(req);
    }
}
