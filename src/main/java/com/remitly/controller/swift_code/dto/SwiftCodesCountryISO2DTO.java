package com.remitly.controller.swift_code.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.util.List;

@Builder
@Getter
@Setter
public class SwiftCodesCountryISO2DTO {
    private String countryISO2;
    private String countryName;

    @Singular
    private List<SwiftCodeBranchDTO> swiftCodes;
}
