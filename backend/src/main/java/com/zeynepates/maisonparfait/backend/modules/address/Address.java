package com.zeynepates.maisonparfait.backend.modules.address;

import com.zeynepates.maisonparfait.backend.modules.common.entity.BaseEntity;
import com.zeynepates.maisonparfait.backend.modules.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "full_name", length = 120)
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