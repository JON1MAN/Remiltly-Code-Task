package com.remitly.dao.model.swift_code;

import com.remitly.dao.model.country.Country;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "swift_codes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SwiftCode {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String countryISO2;

    @Column(nullable = false)
    private String countryName;
    private boolean isHeadquarter;

    @Column(nullable = false, unique = true)
    private String swiftCode;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Builder.Default
    @OneToMany(
            mappedBy = "headquarterId",
            cascade = {CascadeType.ALL}
    )
    private List<SwiftCode> branches = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "headquarter_id")
    private SwiftCode headquarterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country countryId;

    @Transactional
    public void addBranch(SwiftCode swiftCode) {
        this.branches.add(swiftCode);
    }

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
