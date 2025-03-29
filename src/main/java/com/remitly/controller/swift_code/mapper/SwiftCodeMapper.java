package com.remitly.controller.swift_code.mapper;

import com.remitly.controller.swift_code.dto.SwiftCodeBranchDTO;
import com.remitly.controller.swift_code.dto.SwiftCodeDTO;
import com.remitly.controller.swift_code.dto.SwiftCodesCountryISO2DTO;
import com.remitly.dao.model.swift_code.SwiftCode;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;
import java.util.UUID;

public class SwiftCodeMapper {

    public static SwiftCodeDTO map(SwiftCode swiftCode) {
        return SwiftCodeDTO.builder()
                .address(swiftCode.getAddress())
                .bankName(swiftCode.getBankName())
                .countryISO2(swiftCode.getCountryISO2())
                .countryName(swiftCode.getCountryName())
                .isHeadquarter(swiftCode.isHeadquarter())
                .swiftCode(swiftCode.getSwiftCode())
                .branches(swiftCode.getBranches().stream()
                        .filter(code -> !code.isDeleted())
                        .map(SwiftCodeMapper::mapToBranch)
                        .toList()
                )
                .build();
    }

    public static SwiftCode map(SwiftCodeDTO dto) {
        return SwiftCode.builder()
                .id(UUID.randomUUID())
                .countryISO2(dto.getCountryISO2())
                .swiftCode(dto.getSwiftCode())
                .bankName(dto.getBankName())
                .address(dto.getAddress())
                .countryName(dto.getCountryName())
                .isHeadquarter(dto.isHeadquarter())
                .build();
    }

    public static SwiftCodeBranchDTO mapToBranch(SwiftCode branch) {
        return SwiftCodeBranchDTO.builder()
                .address(branch.getAddress())
                .bankName(branch.getBankName())
                .countryISO2(branch.getCountryISO2())
                .isHeadquarter(branch.isHeadquarter())
                .swiftCode(branch.getSwiftCode())
                .build();
    }

    public static SwiftCodesCountryISO2DTO map(List<SwiftCode> swiftCodeList) {
        return SwiftCodesCountryISO2DTO.builder()
                .countryISO2(swiftCodeList.getFirst().getCountryISO2())
                .countryName(swiftCodeList.getFirst().getCountryName())
                .swiftCodes(swiftCodeList.stream()
                        .map(SwiftCodeMapper::mapToBranch)
                        .toList()
                )
                .build();
    }

    public static SwiftCode map(Row row) {
        String swiftCode = row.getCell(1).getStringCellValue().trim().toUpperCase();
        boolean isHeadquarter = swiftCode.endsWith("XXX");

        return SwiftCode.builder()
                .id(UUID.randomUUID())
                .countryISO2(row.getCell(0).getStringCellValue().trim().toUpperCase())
                .swiftCode(swiftCode)
                .bankName(row.getCell(3).getStringCellValue().trim().toUpperCase())
                .address(row.getCell(4).getStringCellValue().trim().toUpperCase())
                .countryName(row.getCell(6).getStringCellValue().trim().toUpperCase())
                .isHeadquarter(isHeadquarter)
                .build();
    }
}
