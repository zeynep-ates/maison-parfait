package com.zeynepates.maisonparfait.backend.modules.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {

    Optional<OrderAddress> findFirstByOrder_IdAndType(Long orderId, OrderAddressType type);
}
