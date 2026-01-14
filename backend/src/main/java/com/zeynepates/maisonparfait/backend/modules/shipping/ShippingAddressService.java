package com.zeynepates.maisonparfait.backend.modules.shipping;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingAddressService {

    private final ShippingAddressInMemoryStore store;

    public Long create(CreateShippingAddressRequest req) {
        ShippingAddress saved = store.save(new ShippingAddress(
                null,
                req.fullName(),
                req.phone(),
                req.city(),
                req.district(),
                req.addressLine()
        ));
        return saved.getId();
    }

    public ShippingAddress getByIdOrThrow(Long id) {
        ShippingAddress a = store.findById(id);
        if (a == null) throw new RuntimeException("Shipping address not found: " + id);
        return a;
    }

    // Snapshot dönmek için:
    public ShippingAddressResponse getSnapshot(Long id) {
        ShippingAddress a = getByIdOrThrow(id);
        return new ShippingAddressResponse(
                a.getFullName(),
                a.getPhone(),
                a.getCity(),
                a.getDistrict(),
                a.getAddressLine()
        );
    }
}
