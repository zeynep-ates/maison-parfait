package com.zeynepates.maisonparfait.backend.modules.address;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/addresses")
public class AddressController {

    private final AddressService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(
            @PathVariable Long userId,
            @Valid @RequestBody CreateAddressRequest req) {
        return service.create(userId, req);
    }

    @GetMapping
    public java.util.List<AddressResponse> list(@PathVariable Long userId) {
        return service.listByUser(userId);
    }

    @GetMapping("/{id}")
    public AddressResponse get(@PathVariable Long userId, @PathVariable Long id) {
        return service.get(userId, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId, @PathVariable Long id) {
        service.delete(userId, id);
    }
}
