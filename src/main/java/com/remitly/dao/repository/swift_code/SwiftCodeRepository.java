package com.remitly.dao.repository.swift_code;

import com.remitly.dao.model.swift_code.SwiftCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SwiftCodeRepository extends JpaRepository<SwiftCode, UUID> {
    Optional<SwiftCode> findById(UUID id);
    Optional<SwiftCode> findBySwiftCode(String swiftCode);
    List<SwiftCode> findByCountryISO2AndIsDeletedFalse(String countryISO2);
    Optional<SwiftCode> findBySwiftCodeAndIsDeletedFalse(String swiftCode);
}
