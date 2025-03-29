package com.remitly.service.swift_code;

import com.remitly.controller.exception.swift_code.SwiftCodeAlreadyExistsException;
import com.remitly.controller.exception.swift_code.SwiftCodeNotFoundException;
import com.remitly.controller.exception.swift_code.SwiftCodeValidationException;
import com.remitly.controller.swift_code.dto.ResponseMessageDTO;
import com.remitly.controller.swift_code.dto.SwiftCodeDTO;
import com.remitly.controller.swift_code.dto.SwiftCodesCountryISO2DTO;
import com.remitly.controller.swift_code.mapper.SwiftCodeMapper;
import com.remitly.dao.model.country.Country;
import com.remitly.dao.model.swift_code.SwiftCode;
import com.remitly.dao.repository.swift_code.SwiftCodeRepository;
import com.remitly.service.country.CountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwiftCodeService {

    private final SwiftCodeRepository swiftCodeRepository;
    private final CountryService countryService;

    private SwiftCode findBySwiftCode(String swiftCode) {
        log.info("Fetching swift code: {}", swiftCode);
        return swiftCodeRepository.findBySwiftCodeAndIsDeletedFalse(swiftCode)
                .orElseThrow(() -> {
                    log.error("There is no such swift code: {}", swiftCode);
                    return new SwiftCodeNotFoundException("Swift code " + swiftCode + ", not found");
                });
    }

    public SwiftCodeDTO getSwiftCode(String code) {
        SwiftCode swiftCode = findBySwiftCode(code);
        return SwiftCodeMapper.map(swiftCode);
    }

    public SwiftCodesCountryISO2DTO getAllSwiftCodesByCountryISO2code(String countryISO2code) {
        log.info("Fetching all swift-codes for countryISO2: {}", countryISO2code);
        var swiftCodes = swiftCodeRepository.findByCountryISO2AndIsDeletedFalse(countryISO2code);
        return SwiftCodeMapper.map(swiftCodes);
    }

    @Transactional
    public ResponseMessageDTO createSwiftCode(SwiftCodeDTO dto) {
        log.info("Creating swift-code {}", dto.getSwiftCode());

        validateSwiftCodeCreation(dto);

        SwiftCode swiftCode = SwiftCodeMapper.map(dto);

        if (!dto.isHeadquarter()) {
            String prefix = dto.getSwiftCode().substring(0, 8);
            var headquarter = swiftCodeRepository.findBySwiftCode(prefix + "XXX");

            headquarter.ifPresent(hq -> {
                swiftCode.setHeadquarterId(hq);
                hq.addBranch(swiftCode);

                swiftCodeRepository.save(hq);
            });

            if (headquarter.isEmpty()) {
                swiftCodeRepository.save(swiftCode);
            }
        } else {
            swiftCodeRepository.save(swiftCode);
        }
        return new ResponseMessageDTO("Swift code " + swiftCode.getSwiftCode() + " created!");
    }

    @Transactional
    public ResponseMessageDTO deleteSwiftCode(String code) {
        log.info("Deleting swift code: {}", code);

        SwiftCode swiftCode = findBySwiftCode(code);
        swiftCode.setDeleted(true);
        swiftCodeRepository.save(swiftCode);

        return new ResponseMessageDTO("Swift code " + code + " deleted!");
    }

    private void validateSwiftCodeCreation(SwiftCodeDTO request) {
        if (swiftCodeAlreadyExists(request.getSwiftCode())) {
            throw new SwiftCodeAlreadyExistsException("Swift code: " + request.getSwiftCode() + ", already exists");
        } else if (request.isHeadquarter() && !isValidHeadQuarterSwiftCode(request.getSwiftCode())) {
            throw new SwiftCodeValidationException(
                    "Invalid format for headquarter provided, should end with XXX, actual: " + request.getSwiftCode()
            );
        } else if (!validISO2CodeWithCountryName(request.getCountryISO2(), request.getCountryName())) {
            throw new SwiftCodeValidationException(
                    "Invalid country combination: ISO2 = '" + request.getCountryISO2() + "', name = '" + request.getCountryName() + "'"
            );
        }
    }

    private boolean validISO2CodeWithCountryName(String countryISO2, String countryName) {
        if (countryISO2 == null || countryName == null) return false;

        for (String iso : Locale.getISOCountries()) {
            if (iso.equalsIgnoreCase(countryISO2)) {
                Locale locale = new Locale.Builder().setRegion(iso).build();
                String displayCountry = locale.getDisplayCountry(Locale.ENGLISH);
                return displayCountry.equalsIgnoreCase(countryName);
            }
        }
        return false;
    }


    private boolean swiftCodeAlreadyExists(String swiftCode) {
        return swiftCodeRepository.findBySwiftCodeAndIsDeletedFalse(swiftCode).isPresent();
    }

    private boolean isValidHeadQuarterSwiftCode(String swiftCode) {
        return swiftCode.endsWith("XXX");
    }
}
