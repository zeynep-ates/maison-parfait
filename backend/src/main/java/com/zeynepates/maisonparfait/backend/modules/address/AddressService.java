package com.zeynepates.maisonparfait.backend.modules.address;

import com.zeynepates.maisonparfait.backend.common.exception.NotFoundException;
import com.zeynepates.maisonparfait.backend.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional
    public AddressResponse create(Long userId, CreateAddressRequest req) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Address a = new Address();
        a.setUser(user);
        a.setTitle(req.title());
        a.setFullName(req.fullName());
        a.setPhone(req.phone());
        a.setCountry(req.country());
        a.setCity(req.city());
        a.setDistrict(req.district());
        a.setAddressLine(req.addressLine());
        a.setPostalCode(req.postalCode());

        return toResponse(addressRepository.save(a));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> listByUser(Long userId) {
        return addressRepository.findByUser_Id(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AddressResponse get(Long userId, Long id) {
        Address a = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Address not found: " + id));

        if (!a.getUser().getId().equals(userId)) {
            throw new NotFoundException("Address not found: " + id); // güvenlik için 404
        }

        return toResponse(a);
    }

    @Transactional(readOnly = true)
    public Address getEntity(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Address not found: " + id));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Address a = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Address not found: " + id));

        if (!a.getUser().getId().equals(userId)) {
            throw new NotFoundException("Address not found: " + id);
        }

        addressRepository.delete(a);
    }

    private AddressResponse toResponse(Address a) {
        return new AddressResponse(
                a.getId(),
                a.getTitle(),
                a.getFullName(),
                a.getPhone(),
                a.getCountry(),
                a.getCity(),
                a.getDistrict(),
                a.getAddressLine(),
                a.getPostalCode()
        );
    }
}