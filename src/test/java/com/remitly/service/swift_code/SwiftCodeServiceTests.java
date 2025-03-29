package com.remitly.service.swift_code;

import com.remitly.controller.exception.swift_code.SwiftCodeAlreadyExistsException;
import com.remitly.controller.exception.swift_code.SwiftCodeNotFoundException;
import com.remitly.controller.exception.swift_code.SwiftCodeValidationException;
import com.remitly.controller.swift_code.dto.ResponseMessageDTO;
import com.remitly.controller.swift_code.dto.SwiftCodeDTO;
import com.remitly.controller.swift_code.dto.SwiftCodesCountryISO2DTO;
import com.remitly.dao.model.swift_code.SwiftCode;
import com.remitly.dao.repository.swift_code.SwiftCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class SwiftCodeServiceTests {

    @Mock
    private SwiftCodeRepository swiftCodeRepository;

    @InjectMocks
    private SwiftCodeService swiftCodeService;

    @Test
    public void testGetBySwiftCode() {
        String code = "BCHICLRMEXZ";
        SwiftCode swiftCode = SwiftCode.builder()
                .swiftCode(code)
                .address("address")
                .bankName("bank")
                .build();

        when(swiftCodeRepository.findBySwiftCodeAndIsDeletedFalse(code))
                .thenReturn(Optional.of(swiftCode));

        SwiftCodeDTO result = swiftCodeService.getSwiftCode(code);

        assertNotNull(result);
        assertEquals(code, result.getSwiftCode());
    }

    @Test
    public void testGetBySwiftCode_notFound() {
        String code = "BCHICLRMEXZ";
        when(swiftCodeRepository.findBySwiftCodeAndIsDeletedFalse(code))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                SwiftCodeNotFoundException.class,
                () -> swiftCodeService.getSwiftCode(code)
        );

        assertEquals("Swift code " + code + ", not found", exception.getMessage());
    }

    @Test
    public void testGetAllSwiftCodesByCountryISO2code() {
        String countryISO2 = "PL";
        List<SwiftCode> swiftCodeList = new ArrayList<>(List.of(
                SwiftCode.builder()
                        .countryISO2("PL")
                        .countryName("POLAND")
                        .swiftCode("ABCDEFGHIJK")
                        .isHeadquarter(false)
                        .build(),
                SwiftCode.builder()
                        .countryISO2("PL")
                        .countryName("POLAND")
                        .swiftCode("AACDEFGHIJK")
                        .isHeadquarter(false)
                        .build()
        ));

        when(swiftCodeRepository.findByCountryISO2AndIsDeletedFalse(countryISO2))
                .thenReturn(swiftCodeList);

        SwiftCodesCountryISO2DTO result = swiftCodeService
                .getAllSwiftCodesByCountryISO2code(countryISO2);

        assertNotNull(result);
        assertEquals("POLAND", result.getCountryName());
        assertEquals("PL", result.getCountryISO2());
        verify(swiftCodeRepository, times(1)).findByCountryISO2AndIsDeletedFalse(countryISO2);
    }

    @Test
    void testCreateSwiftCode_asHeadquarter() {
        SwiftCodeDTO dto = SwiftCodeDTO.builder()
                .swiftCode("ABCDEFGHXXX")
                .isHeadquarter(true)
                .countryName("Poland")
                .countryISO2("PL")
                .build();
        SwiftCode entity = SwiftCode.builder()
                .swiftCode("ABCDEFGHXXX")
                .isHeadquarter(true)
                .build();

        when(swiftCodeRepository.save(any(SwiftCode.class)))
                .thenReturn(entity);

        ResponseMessageDTO result = swiftCodeService.createSwiftCode(dto);

        verify(swiftCodeRepository).save(any(SwiftCode.class));
        assertEquals("Swift code ABCDEFGHXXX created!", result.getMessage());
    }

    @Test
    void testCreateSwiftCode_asBranch_withExistingHeadquarter() {
        SwiftCodeDTO dto = SwiftCodeDTO.builder()
                .swiftCode("ABCDEFGH001")
                .countryName("Poland")
                .countryISO2("PL")
                .isHeadquarter(false)
                .build();

        SwiftCode branch = SwiftCode.builder()
                .swiftCode("ABCDEFGH001")
                .countryName("Poland")
                .countryISO2("PL")
                .isHeadquarter(false)
                .build();

        SwiftCode headquarter = SwiftCode.builder()
                .swiftCode("ABCDEFGHXXX")
                .countryName("Poland")
                .countryISO2("PL")
                .isHeadquarter(true)
                .build();

        when(swiftCodeRepository.findBySwiftCode("ABCDEFGHXXX"))
                .thenReturn(Optional.of(headquarter));
        when(swiftCodeRepository.save(headquarter)).thenReturn(headquarter);

        ResponseMessageDTO result = swiftCodeService.createSwiftCode(dto);

        verify(swiftCodeRepository).save(headquarter);
        assertEquals("Swift code " + branch.getSwiftCode() + " created!", result.getMessage());
    }


    @Test
    void testCreateSwiftCode_asBranch_withoutHeadquarter() {
        SwiftCodeDTO dto = SwiftCodeDTO.builder()
                .swiftCode("ABCDEFGH001")
                .countryName("Poland")
                .countryISO2("PL")
                .isHeadquarter(false)
                .build();

        SwiftCode branch = SwiftCode.builder()
                .swiftCode("ABCDEFGH001")
                .countryName("Poland")
                .countryISO2("PL")
                .isHeadquarter(false)
                .build();

        when(swiftCodeRepository.findBySwiftCode("ABCDEFGHXXX"))
                .thenReturn(Optional.empty());

        when(swiftCodeRepository.save(any(SwiftCode.class))).thenReturn(branch);

        ResponseMessageDTO result = swiftCodeService.createSwiftCode(dto);

        verify(swiftCodeRepository).save(any(SwiftCode.class));
        assertEquals("Swift code " + branch.getSwiftCode() + " created!", result.getMessage());
    }

    @Test
    void testCreateSwiftCode_swiftCodeAlreadyExistsException() {
        SwiftCodeDTO dto = SwiftCodeDTO.builder()
                .swiftCode("ABCDEFGH001")
                .isHeadquarter(false)
                .build();

        SwiftCode branch = SwiftCode.builder()
                .swiftCode("ABCDEFGH001")
                .isHeadquarter(false)
                .build();

        when(swiftCodeRepository.findBySwiftCodeAndIsDeletedFalse("ABCDEFGH001"))
                .thenReturn(Optional.of(branch));

        var exception = assertThrows(
                SwiftCodeAlreadyExistsException.class,
                () -> swiftCodeService.createSwiftCode(dto));

        verify(swiftCodeRepository).findBySwiftCodeAndIsDeletedFalse("ABCDEFGH001");
        assertEquals("Swift code: ABCDEFGH001, already exists", exception.getMessage());
    }

    @Test
    void testCreateSwiftCode_SwiftCodeValidationException() {
        String invalidSwiftCode = "ABCDEFGH001";
        SwiftCodeDTO dto = SwiftCodeDTO.builder()
                .swiftCode(invalidSwiftCode)
                .isHeadquarter(true)
                .build();

        when(swiftCodeRepository.findBySwiftCodeAndIsDeletedFalse(invalidSwiftCode))
                .thenReturn(Optional.empty());

        SwiftCodeValidationException exception = assertThrows(
                SwiftCodeValidationException.class,
                () -> swiftCodeService.createSwiftCode(dto)
        );

        assertEquals(
                "Invalid format for headquarter provided, should end with XXX, actual: ABCDEFGH001",
                exception.getMessage()
        );

        verify(swiftCodeRepository).findBySwiftCodeAndIsDeletedFalse(invalidSwiftCode);
        verifyNoMoreInteractions(swiftCodeRepository);
    }

    @Test
    void testCreateSwiftCode_invalidCountryCombination_throwsValidationException() {
        SwiftCodeDTO dto = SwiftCodeDTO.builder()
                .swiftCode("ABCDEFGHXXX")
                .isHeadquarter(true)
                .countryISO2("PL")
                .countryName("England")
                .build();

        when(swiftCodeRepository.findBySwiftCodeAndIsDeletedFalse("ABCDEFGHXXX"))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                SwiftCodeValidationException.class,
                () -> swiftCodeService.createSwiftCode(dto)
        );

        verify(swiftCodeRepository).findBySwiftCodeAndIsDeletedFalse("ABCDEFGHXXX");
        assertEquals("Invalid country combination: ISO2 = 'PL', name = 'England'", exception.getMessage());
    }



    @Test
    void testDeleteSwiftCode() {
        String swiftCodeStr = "ABCDEFGHXXX";
        SwiftCode swiftCode = SwiftCode.builder()
                .swiftCode(swiftCodeStr)
                .isDeleted(false)
                .build();

        when(swiftCodeRepository.findBySwiftCodeAndIsDeletedFalse(swiftCodeStr))
                .thenReturn(Optional.of(swiftCode));

        ResponseMessageDTO response = swiftCodeService.deleteSwiftCode(swiftCodeStr);

        verify(swiftCodeRepository).save(swiftCode);
        assertTrue(swiftCode.isDeleted());
        assertEquals("Swift code ABCDEFGHXXX deleted!", response.getMessage());
    }

    @Test
    void testDeleteSwiftCode_notFound() {
        String swiftCodeStr = "ABCDEFGHXXX";
        SwiftCode swiftCode = SwiftCode.builder()
                .swiftCode(swiftCodeStr)
                .isDeleted(false)
                .build();

        when(swiftCodeRepository.findBySwiftCodeAndIsDeletedFalse(swiftCodeStr))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                SwiftCodeNotFoundException.class,
                () -> swiftCodeService.deleteSwiftCode(swiftCodeStr)
        );

        verify(swiftCodeRepository, never()).save(any());
        assertEquals("Swift code " + swiftCodeStr + ", not found", exception.getMessage());
    }
}
