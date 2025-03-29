package com.remitly.controller.swift_code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.util.List;

@Builder
@Getter
@Setter
public class SwiftCodeDTO {
    private String address;
    private String bankName;
    private String countryISO2;
    private String countryName;

    @JsonProperty("isHeadquarter")
    private boolean isHeadquarter;
    private String swiftCode;

    @Singular
    private List<SwiftCodeBranchDTO> branches;
}