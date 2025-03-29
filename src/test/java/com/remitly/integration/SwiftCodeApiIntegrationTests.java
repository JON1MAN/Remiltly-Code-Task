package com.remitly.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remitly.controller.exception.ExceptionResponseDTO;
import com.remitly.controller.swift_code.dto.ResponseMessageDTO;
import com.remitly.controller.swift_code.dto.SwiftCodeBranchDTO;
import com.remitly.controller.swift_code.dto.SwiftCodeDTO;
import com.remitly.controller.swift_code.dto.SwiftCodesCountryISO2DTO;
import com.remitly.dao.model.swift_code.SwiftCode;
import com.remitly.dao.repository.swift_code.SwiftCodeRepository;
import com.remitly.service.swift_code.SwiftCodeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SwiftCodeApiIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SwiftCodeRepository swiftCodeRepository;

    @Autowired
    private SwiftCodeService swiftCodeService;

    private static HttpHeaders headers;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void init() {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    private String createURLWithPort() {
        return "http://localhost:" + port + "/v1/swift-codes";
    }

    @Test
    @Sql(statements = "INSERT INTO swift_codes(id, address, bank_name, countryiso2, country_name, is_headquarter, swift_code, is_deleted)" +
            " VALUES ('92fc1126-f6f1-4696-bd12-549292972c1a', 'address', 'bank', 'PL', 'POLAND', false, '-----------', false)"
            , executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM swift_codes WHERE swift_code='-----------'"
            , executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testGetSwiftCode_branch() throws JsonProcessingException {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<SwiftCodeDTO> response = restTemplate.exchange(
                (createURLWithPort() + "/-----------"), HttpMethod.GET, entity, SwiftCodeDTO.class);

        var result = response.getBody();
        var expected = SwiftCodeDTO.builder()
                .address("address")
                .bankName("bank")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .swiftCode("-----------")
                .build();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(result);

        assertEquals(expected.getSwiftCode(), result.getSwiftCode());
        assertEquals(expected.getBankName(), result.getBankName());
        assertEquals(expected.getAddress(), result.getAddress());
        assertEquals(expected.getCountryISO2(), result.getCountryISO2());
        assertEquals(expected.getCountryName(), result.getCountryName());
        assertEquals(expected.isHeadquarter(), result.isHeadquarter());
    }

    @Test
    @Sql(statements = "INSERT INTO swift_codes(id, address, bank_name, countryiso2, country_name, is_headquarter, swift_code, is_deleted)" +
            " VALUES ('92fc1126-f6f1-4696-bd12-549292972c1a', 'address', 'bank', 'PL', 'POLAND', false, '--------XXX', false)"
            , executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "INSERT INTO swift_codes(id, address, bank_name, countryiso2, country_name, is_headquarter, swift_code, is_deleted, headquarter_id)" +
            " VALUES ('71a8b3c5-bc72-40ab-b5fc-230b7fe07239', 'address', 'bank', 'PL', 'POLAND', false, '-----------', false, '92fc1126-f6f1-4696-bd12-549292972c1a')"
            , executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM swift_codes WHERE swift_code='-----------'"
            , executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql(statements = "DELETE FROM swift_codes WHERE swift_code='--------XXX'"
            , executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testGetSwiftCode_headquarter() throws JsonProcessingException {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<SwiftCodeDTO> response = restTemplate.exchange(
                (createURLWithPort() + "/--------XXX"), HttpMethod.GET, entity, SwiftCodeDTO.class);

        var result = response.getBody();
        var expected = SwiftCodeDTO.builder()
                .address("address")
                .bankName("bank")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .swiftCode("--------XXX")
                .branches(List.of(SwiftCodeBranchDTO.builder()
                                .swiftCode("-----------")
                                .isHeadquarter(false)
                                .bankName("bank")
                                .countryISO2("PL")
                        .build())
                )
                .build();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(result);

        assertEquals(expected.getSwiftCode(), result.getSwiftCode());
        assertEquals(expected.getBankName(), result.getBankName());
        assertEquals(expected.getAddress(), result.getAddress());
        assertEquals(expected.getCountryISO2(), result.getCountryISO2());
        assertEquals(expected.getCountryName(), result.getCountryName());
        assertEquals(expected.isHeadquarter(), result.isHeadquarter());
        assertEquals(expected.getBranches().size(), result.getBranches().size());
        assertEquals(expected.getBranches().getFirst().isHeadquarter(), result.getBranches().getFirst().isHeadquarter());
        assertEquals(expected.getBranches().getFirst().getSwiftCode(), result.getBranches().getFirst().getSwiftCode());
        assertEquals(expected.getBranches().getFirst().getBankName(), result.getBranches().getFirst().getBankName());
        assertEquals(expected.getBranches().getFirst().getCountryISO2(), result.getBranches().getFirst().getCountryISO2());
    }

    @Test
    public void testGetSwiftCode_returnErrorResponseOnNotFound() throws JsonProcessingException {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        var response = restTemplate.exchange(
                (createURLWithPort() + "/-----------"), HttpMethod.GET, entity, ExceptionResponseDTO.class);

        var result = response.getBody();

        assertEquals(response.getStatusCode().value(), 404);
        assertNotNull(result);
        assertEquals("Swift code -----------, not found", result.getMessage());
        assertEquals(404, result.getStatus());
        assertEquals("Not Found", result.getError());
    }

    @Test
    @Sql(statements = "INSERT INTO swift_codes(id, address, bank_name, countryiso2, country_name, is_headquarter, swift_code, is_deleted)" +
            " VALUES ('92fc1126-f6f1-4696-bd12-549292972c1a', 'address', 'bank', 'PL', 'POLAND', false, '-----------', false)"
            , executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM swift_codes WHERE swift_code='-----------'"
            , executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testGetAllSwiftCodesByCountryISO2code() throws JsonProcessingException {
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        var response = restTemplate.exchange(
                (createURLWithPort() + "/country/PL"),
                HttpMethod.GET,
                entity,
                SwiftCodesCountryISO2DTO.class);

        var result = response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(result);
        assertEquals(swiftCodeRepository.findByCountryISO2AndIsDeletedFalse("PL").size(), result.getSwiftCodes().size());
        assertEquals(swiftCodeService.getAllSwiftCodesByCountryISO2code("PL").getCountryISO2(), result.getCountryISO2());
        assertEquals(swiftCodeService.getAllSwiftCodesByCountryISO2code("PL").getCountryName(), result.getCountryName());
        assertEquals(swiftCodeService.getAllSwiftCodesByCountryISO2code("PL").getSwiftCodes().size(), result.getSwiftCodes().size());
    }

    @Test
    @Sql(statements = "DELETE FROM swift_codes WHERE swift_code='-----------'"
            , executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testCreateSwiftCode() throws JsonProcessingException {
        SwiftCodeDTO requestBody = SwiftCodeDTO.builder()
                .address("address")
                .bankName("bank")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .swiftCode("-----------")
                .build();

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        var response = restTemplate.exchange(
                createURLWithPort(),
                HttpMethod.POST,
                entity,
                ResponseMessageDTO.class);

        var result = Objects.requireNonNull(response.getBody());

        assertEquals(201, response.getStatusCode().value());
        assertEquals("Swift code " + requestBody.getSwiftCode() + " created!", result.getMessage());
    }

    @Test
    @Sql(statements = "INSERT INTO swift_codes(id, address, bank_name, countryiso2, country_name, is_headquarter, swift_code, is_deleted)" +
            " VALUES ('92fc1126-f6f1-4696-bd12-549292972c1a', 'address', 'bank', 'PL', 'POLAND', false, '-----------', false)"
            , executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM swift_codes WHERE swift_code='-----------'"
            , executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testCreateSwiftCode_returnErrorResponseOnConflict() throws JsonProcessingException {
        SwiftCodeDTO requestBody = SwiftCodeDTO.builder()
                .address("address")
                .bankName("bank")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .swiftCode("-----------")
                .build();

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        var response = restTemplate.exchange(
                createURLWithPort(),
                HttpMethod.POST,
                entity,
                ExceptionResponseDTO.class);

        var result = Objects.requireNonNull(response.getBody());

        assertEquals(response.getStatusCode().value(), 409);
        assertNotNull(result);
        assertEquals("Swift code: -----------, already exists", result.getMessage());
        assertEquals(409, result.getStatus());
        assertEquals("Conflict", result.getError());
    }

    @Test
    @Sql(statements = "DELETE FROM swift_codes WHERE swift_code='-----------'"
            , executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testCreateSwiftCode_returnErrorResponseOnBadRequest() throws JsonProcessingException {
        SwiftCodeDTO requestBody = SwiftCodeDTO.builder()
                .address("address")
                .bankName("bank")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .swiftCode("-----------")
                .build();

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        var response = restTemplate.exchange(
                createURLWithPort(),
                HttpMethod.POST,
                entity,
                ExceptionResponseDTO.class);

        var result = Objects.requireNonNull(response.getBody());

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(result);
        assertEquals("Invalid format for headquarter provided, should end with XXX, actual: -----------", result.getMessage());
        assertEquals(400, result.getStatus());
        assertEquals("Bad Request", result.getError());
    }

    @Test
    @Sql(statements = "DELETE FROM swift_codes WHERE swift_code='--------XXX'",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testCreateSwiftCode_invalidCountryCombination_returnsBadRequest() throws JsonProcessingException {
        SwiftCodeDTO requestBody = SwiftCodeDTO.builder()
                .address("address")
                .bankName("bank")
                .countryISO2("PL")
                .countryName("England")
                .isHeadquarter(true)
                .swiftCode("--------XXX")
                .build();

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        var response = restTemplate.exchange(
                createURLWithPort(),
                HttpMethod.POST,
                entity,
                ExceptionResponseDTO.class
        );

        var result = Objects.requireNonNull(response.getBody());

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(result);
        assertEquals("Invalid country combination: ISO2 = 'PL', name = 'England'", result.getMessage());
        assertEquals(400, result.getStatus());
        assertEquals("Bad Request", result.getError());
    }


    @Test
    @Sql(statements = "INSERT INTO swift_codes(id, address, bank_name, countryiso2, country_name, is_headquarter, swift_code, is_deleted)" +
            " VALUES ('92fc1126-f6f1-4696-bd12-549292972c1a', 'address', 'bank', 'PL', 'POLAND', false, '-----------', false)"
            , executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM swift_codes WHERE swift_code='-----------'"
            , executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testDeleteSwiftCode() throws JsonProcessingException {
        var response = restTemplate.exchange(
                (createURLWithPort() + "/-----------"), HttpMethod.DELETE, null, ResponseMessageDTO.class);
        var result = response.getBody();

        assertEquals(response.getStatusCode().value(), 200);
        assertNotNull(result);
        assertEquals(result.getMessage(), "Swift code ----------- deleted!");
    }

    @Test
    public void testDeleteSwiftCode_returnErrorResponseOnNotFound() throws JsonProcessingException {
        var response = restTemplate.exchange(
                (createURLWithPort() + "/-----------"), HttpMethod.DELETE, null, ExceptionResponseDTO.class);
        var result = response.getBody();

        assertEquals(response.getStatusCode().value(), 404);
        assertNotNull(result);
        assertEquals("Swift code -----------, not found", result.getMessage());
        assertEquals(404, result.getStatus());
        assertEquals("Not Found", result.getError());
    }





}
