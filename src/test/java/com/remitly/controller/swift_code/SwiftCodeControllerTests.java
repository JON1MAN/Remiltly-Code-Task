package com.remitly.controller.swift_code;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remitly.controller.exception.swift_code.SwiftCodeAlreadyExistsException;
import com.remitly.controller.exception.swift_code.SwiftCodeNotFoundException;
import com.remitly.controller.exception.swift_code.SwiftCodeValidationException;
import com.remitly.controller.swift_code.controller.SwiftCodeController;
import com.remitly.controller.swift_code.dto.ResponseMessageDTO;
import com.remitly.controller.swift_code.dto.SwiftCodeBranchDTO;
import com.remitly.controller.swift_code.dto.SwiftCodeDTO;
import com.remitly.controller.swift_code.dto.SwiftCodesCountryISO2DTO;
import com.remitly.service.parser.ExcelParserService;
import com.remitly.service.swift_code.SwiftCodeService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;


@WebMvcTest(SwiftCodeController.class)
public class SwiftCodeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SwiftCodeService swiftCodeService;

    @MockitoBean
    private ExcelParserService excelParserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetSwiftCodeHeadquarter() throws Exception {
        SwiftCodeDTO swiftCodeDTO = SwiftCodeDTO.builder()
                .address("address")
                .bankName("bank")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .swiftCode("BCHICLRMXXX")
                .branches(List.of(
                        SwiftCodeBranchDTO.builder()
                                .address("address")
                                .bankName("bank")
                                .countryISO2("PL")
                                .isHeadquarter(false)
                                .swiftCode("BCHICLRMEXX")
                                .build(),
                        SwiftCodeBranchDTO.builder()
                                .address("address")
                                .bankName("bank")
                                .countryISO2("PL")
                                .isHeadquarter(false)
                                .swiftCode("BCHICLRMXEX")
                                .build(),
                        SwiftCodeBranchDTO.builder()
                                .address("address")
                                .bankName("bank")
                                .countryISO2("PL")
                                .isHeadquarter(false)
                                .swiftCode("BCHICLRMXXE")
                                .build()
                ))
                .build();
        when(swiftCodeService.getSwiftCode("BCHICLRMXXX"))
                .thenReturn(swiftCodeDTO);

        mockMvc.perform(get("/v1/swift-codes/" + swiftCodeDTO.getSwiftCode()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.address", is(swiftCodeDTO.getAddress())))
                .andExpect(jsonPath("$.bankName", is(swiftCodeDTO.getBankName())))
                .andExpect(jsonPath("$.countryISO2", is(swiftCodeDTO.getCountryISO2())))
                .andExpect(jsonPath("$.countryName", is(swiftCodeDTO.getCountryName())))
                .andExpect(jsonPath("$.isHeadquarter", is(swiftCodeDTO.isHeadquarter())))
                .andExpect(jsonPath("$.swiftCode", is(swiftCodeDTO.getSwiftCode())))
                .andExpect(jsonPath("$.branches").isArray())
                .andExpect(jsonPath("$.branches").isNotEmpty())
                .andExpect(jsonPath("$.branches", hasSize(3)));

    }

    @Test
    public void testGetSwiftCodeBranch() throws Exception {
        SwiftCodeDTO swiftCodeDTO = SwiftCodeDTO.builder()
                .address("address")
                .bankName("bank")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .swiftCode("BCHICLRMEXZ")
                .build();
        when(swiftCodeService.getSwiftCode("BCHICLRMEXZ"))
                .thenReturn(swiftCodeDTO);

        mockMvc.perform(get("/v1/swift-codes/" + swiftCodeDTO.getSwiftCode()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.address", is(swiftCodeDTO.getAddress())))
                .andExpect(jsonPath("$.bankName", is(swiftCodeDTO.getBankName())))
                .andExpect(jsonPath("$.countryISO2", is(swiftCodeDTO.getCountryISO2())))
                .andExpect(jsonPath("$.countryName", is(swiftCodeDTO.getCountryName())))
                .andExpect(jsonPath("$.isHeadquarter", is(swiftCodeDTO.isHeadquarter())))
                .andExpect(jsonPath("$.swiftCode", is(swiftCodeDTO.getSwiftCode())))
                .andExpect(jsonPath("$.branches").isEmpty());
    }

    @Test
    public void testGetSwiftCode_notFound() throws Exception {
        String swiftCode = "ABCDEFGHIJK";

        when(swiftCodeService.getSwiftCode(swiftCode))
                .thenThrow(new SwiftCodeNotFoundException("Swift code " + swiftCode + ", not found"));

        mockMvc.perform(get("/v1/swift-codes/" + swiftCode))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Swift code " + swiftCode + ", not found")));
    }

    @Test
    public void testGetAllSwiftCodesByCountryISO2code() throws Exception {
        SwiftCodesCountryISO2DTO response = SwiftCodesCountryISO2DTO.builder()
                .countryISO2("PL")
                .countryName("POLAND")
                .swiftCodes(List.of(
                        SwiftCodeBranchDTO.builder()
                                .address("address")
                                .bankName("bank")
                                .countryISO2("PL")
                                .isHeadquarter(false)
                                .swiftCode("BCHICLRMEXX")
                                .build(),
                        SwiftCodeBranchDTO.builder()
                                .address("address")
                                .bankName("bank")
                                .countryISO2("PL")
                                .isHeadquarter(false)
                                .swiftCode("BCHICLRMXEX")
                                .build(),
                        SwiftCodeBranchDTO.builder()
                                .address("address")
                                .bankName("bank")
                                .countryISO2("PL")
                                .isHeadquarter(false)
                                .swiftCode("BCHICLRMXXE")
                                .build()
                ))
                .build();
        when(swiftCodeService.getAllSwiftCodesByCountryISO2code(response.getCountryISO2()))
                .thenReturn(response);

        mockMvc.perform(get("/v1/swift-codes/country/" + response.getCountryISO2()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.countryISO2", is(response.getCountryISO2())))
                .andExpect(jsonPath("$.countryName", is(response.getCountryName())))
                .andExpect(jsonPath("$.swiftCodes").isNotEmpty())
                .andExpect(jsonPath("$.swiftCodes", hasSize(3)));
    }

    @Test
    public void testCreateSwiftCode() throws Exception {
        SwiftCodeDTO request = SwiftCodeDTO.builder()
                .address("address")
                .bankName("bank")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .swiftCode("BCHICLRMXXX")
                .build();

        ResponseMessageDTO response = new ResponseMessageDTO(
                "Swift code " + request.getSwiftCode() + " created!");

        when(swiftCodeService.createSwiftCode(ArgumentMatchers.any()))
                .thenReturn(response);

        mockMvc.perform(
                post("/v1/swift-codes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(response.getMessage())));
    }

    @Test
    public void testCreateSwiftCode_alreadyExists() throws Exception {
        SwiftCodeDTO request = SwiftCodeDTO.builder()
                .address("address")
                .bankName("bank")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .swiftCode("BCHICLRMXXX")
                .build();

        when(swiftCodeService.createSwiftCode(ArgumentMatchers.any()))
                .thenThrow(new SwiftCodeAlreadyExistsException(
                        "Swift code: " + request.getSwiftCode() + ", already exists")
                );

        mockMvc.perform(post("/v1/swift-codes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "Swift code: " + request.getSwiftCode() + ", already exists"));
    }

    @Test
    public void testCreateSwiftCode_invalidCountryCombination() throws Exception {
        SwiftCodeDTO request = SwiftCodeDTO.builder()
                .address("address")
                .bankName("bank")
                .countryISO2("PL")
                .countryName("England")
                .isHeadquarter(true)
                .swiftCode("BCHICLRMXXX")
                .build();

        when(swiftCodeService.createSwiftCode(ArgumentMatchers.any()))
                .thenThrow(new SwiftCodeValidationException(
                        "Invalid country combination: ISO2 = 'PL', name = 'England'")
                );

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "Invalid country combination: ISO2 = 'PL', name = 'England'"));
    }


    @Test
    public void testCreateSwiftCode_validationForHeadquarter() throws Exception {
        SwiftCodeDTO request = SwiftCodeDTO.builder()
                .address("address")
                .bankName("bank")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .swiftCode("---------XX")
                .build();

        when(swiftCodeService.createSwiftCode(ArgumentMatchers.any()))
                .thenThrow(new SwiftCodeValidationException(
                        "Invalid format for headquarter provided, should end with XXX, actual: " + request.getSwiftCode())
                );

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "Invalid format for headquarter provided, should end with XXX, actual: ---------XX"));
    }

    @Test
    public void testDeleteSwiftCode() throws Exception {
        String swiftCode = "ABCDEFGHIGK";

        ResponseMessageDTO response = new ResponseMessageDTO(
                "Swift code " + swiftCode + " deleted!");

        when(swiftCodeService.deleteSwiftCode(swiftCode))
                .thenReturn(response);

        mockMvc.perform(
                        delete("/v1/swift-codes/ABCDEFGHIGK")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(response.getMessage())));
    }

    @Test
    public void testDeleteSwiftCode_notFound() throws Exception {
        String swiftCode = "ABCDEFGHIGK";

        when(swiftCodeService.deleteSwiftCode(swiftCode))
                .thenThrow(new SwiftCodeNotFoundException("Swift code " + swiftCode + ", not found"));

        mockMvc.perform(
                        delete("/v1/swift-codes/ABCDEFGHIGK")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Swift code " + swiftCode + ", not found")));
    }


}
