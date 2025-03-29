package com.remitly.repository;

import com.remitly.dao.model.swift_code.SwiftCode;
import com.remitly.dao.repository.swift_code.SwiftCodeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class SwiftCodeRepositoryTests {

    @Autowired
    private SwiftCodeRepository swiftCodeRepository;

    @BeforeEach
    public void setUp() {
        swiftCodeRepository.save(SwiftCode.builder()
                .id(UUID.fromString("a85b3d3e-f47b-407e-8a70-3f9c7a37d035"))
                .address("address")
                .swiftCode("ABCDEFGHIJK")
                .bankName("BANK")
                .isHeadquarter(false)
                .countryISO2("PL")
                .countryName("POLAND")
                .build()
        );
        swiftCodeRepository.save(SwiftCode.builder()
                .id(UUID.fromString("213bd286-071b-4fd8-9385-699563bd3594"))
                .address("address")
                .swiftCode("ABCDEFGHXXX")
                .bankName("BANK")
                .isHeadquarter(true)
                .countryISO2("PL")
                .countryName("POLAND")
                .build()
        );
    }

    @AfterEach
    public void destroy() {
        swiftCodeRepository.deleteAll();
    }

    @Test
    public void testFindAll() {
        List<SwiftCode> swiftCodes = swiftCodeRepository.findAll();
        swiftCodes.sort(Comparator.comparing(SwiftCode::getSwiftCode));
        assertThat(swiftCodes.size()).isEqualTo(2);
        assertThat(swiftCodes.get(0).getSwiftCode()).isEqualTo("ABCDEFGHIJK");
        assertThat(swiftCodes.get(1).getSwiftCode()).isEqualTo("ABCDEFGHXXX");
    }

    @Test
    public void testFindById() {
        UUID id = UUID.fromString("a85b3d3e-f47b-407e-8a70-3f9c7a37d035");
        var result = swiftCodeRepository.findById(id);

        assertThat(result).isNotEmpty();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    public void testFindById_noSuchElementException() {
        var result = assertThrows(
                NoSuchElementException.class,
                () -> swiftCodeRepository.findById(UUID.randomUUID()).get()
        );

        assertThat(result).isNotNull();
        assertThat(result.getClass()).isEqualTo(NoSuchElementException.class);
        assertThat(result.getMessage()).isEqualTo("No value present");
    }

    @Test
    public void testFindBySwiftCode() {
        String code = "ABCDEFGHIJK";

        Optional<SwiftCode> result = swiftCodeRepository.findBySwiftCode(code);

        assertThat(result).isPresent();
        assertThat(result.get().getSwiftCode()).isEqualTo(code);
    }


    @Test
    public void testFindBySwiftCode_noSuchElementException() {
        String code = "ABCDEFGHIJB";

        var result = assertThrows(
                NoSuchElementException.class,
                () -> swiftCodeRepository.findBySwiftCode(code).get()
        );

        assertThat(result).isNotNull();
        assertThat(result.getClass()).isEqualTo(NoSuchElementException.class);
        assertThat(result.getMessage()).isEqualTo("No value present");
    }

    @Test
    public void testFindByCountryISO2AndIsDeletedFalse() {
        String code = "PL";

        var result = swiftCodeRepository.findByCountryISO2AndIsDeletedFalse(code);

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void testFindBySwiftCodeAndIsDeletedFalse() {
        String code = "ABCDEFGHIJK";

        var result = swiftCodeRepository.findBySwiftCodeAndIsDeletedFalse(code);

        assertThat(result).isNotEmpty();
        assertThat(result.get().getSwiftCode()).isEqualTo(code);
    }

    @Test
    public void testFindBySwiftCodeAndIsDeletedFalse_noSuchElementException() {
        String code = "ABCDEFGHIJB";
        var result = assertThrows(
                NoSuchElementException.class,
                () -> swiftCodeRepository.findBySwiftCodeAndIsDeletedFalse(code).get()
        );

        assertThat(result).isNotNull();
        assertThat(result.getClass()).isEqualTo(NoSuchElementException.class);
        assertThat(result.getMessage()).isEqualTo("No value present");
    }



}
