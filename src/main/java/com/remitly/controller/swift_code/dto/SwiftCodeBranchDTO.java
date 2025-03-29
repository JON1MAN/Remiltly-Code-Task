package com.remitly.controller.swift_code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SwiftCodeBranchDTO {
    private String address;
    private String bankName;
    private String countryISO2;

    @JsonProperty("isHeadquarter")
    private boolean isHeadquarter;
    private String swiftCode;
}
