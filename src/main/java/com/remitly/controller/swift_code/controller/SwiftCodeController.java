package com.remitly.controller.swift_code.controller;

import com.remitly.controller.swift_code.dto.ResponseMessageDTO;
import com.remitly.controller.swift_code.dto.SwiftCodeDTO;
import com.remitly.controller.swift_code.dto.SwiftCodesCountryISO2DTO;
import com.remitly.service.parser.ExcelParserService;
import com.remitly.service.swift_code.SwiftCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/swift-codes")
@RequiredArgsConstructor
public class SwiftCodeController {

    private final ExcelParserService excelParserService;
    private final SwiftCodeService swiftCodeService;

    @GetMapping("/{swift-code}")
    public ResponseEntity<SwiftCodeDTO> getSwiftCode(
            @PathVariable("swift-code") String swiftCode
    ) {
        log.info("Received a request to get a swift code: {}", swiftCode);
        return ResponseEntity.ok(swiftCodeService.getSwiftCode(swiftCode));
    }

    @GetMapping("/country/{countryISO2code}")
    public ResponseEntity<SwiftCodesCountryISO2DTO> getAllSwiftCodesByCountryISO2code(
            @PathVariable("countryISO2code") String countryISO2code
    ) {
        log.info("Received a request to get all swift codes by countryISO2code: {}", countryISO2code);
        return ResponseEntity.ok(swiftCodeService.getAllSwiftCodesByCountryISO2code(countryISO2code.toUpperCase()));
    }

    @PostMapping
    public ResponseEntity<ResponseMessageDTO> createSwiftCode(@RequestBody SwiftCodeDTO request) {
        log.info("Received a request to create swift code: {}", request.getSwiftCode());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(swiftCodeService.createSwiftCode(request));
    }

    @DeleteMapping("/{swift-code}")
    public ResponseEntity<ResponseMessageDTO> deleteSwiftCode(
            @PathVariable("swift-code") String swiftCode
    ) {
        log.info("Received a request to delete a swift code: {}", swiftCode);
        return ResponseEntity.ok(swiftCodeService.deleteSwiftCode(swiftCode));
    }

    /*@PostMapping("/parser")
    public ResponseEntity<String> parseExcelFile() {
        log.info("Received request to parse excel file");
        excelParserService.parseFile();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("successfully parsed and saved data to database :)");
    }*/
}
