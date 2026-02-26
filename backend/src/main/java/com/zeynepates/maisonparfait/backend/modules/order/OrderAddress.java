package com.zeynepates.maisonparfait.backend.modules.order;

import com.zeynepates.maisonparfait.backend.modules.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_addresses")
public class OrderAddress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private OrderAddressType type;

    @Column(name = "full_name", length = 120, nullable = false)
    private String fullName;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "country", length = 100, nullable = false)
    private String country;

    @Column(name = "city", length = 100, nullable = false)
    private String city;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "address_line", length = 500, nullable = false)
    private String addressLine;

    @Column(name = "postal_code", length = 20)
    private String postalCode;
}
