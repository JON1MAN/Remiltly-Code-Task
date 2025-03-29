package com.remitly.service.parser;

import com.remitly.dao.model.swift_code.SwiftCode;
import com.remitly.dao.repository.swift_code.SwiftCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import static com.remitly.controller.swift_code.mapper.SwiftCodeMapper.map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelParserService {
    private final SwiftCodeRepository swiftCodeRepository;
    private static final String EXCEL_FILE_PATH = "path_to_file_to_parse";

    @Transactional
    public void parseFile() {
        log.info("Start parsing of excel document: {}", EXCEL_FILE_PATH);

        List<SwiftCode> swiftCodes = new ArrayList<>();
        Map<String, SwiftCode> headquartersMap = new HashMap<>();

        try (InputStream inputStream = new FileInputStream(
                Paths.get(EXCEL_FILE_PATH).toFile())
        ) {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                SwiftCode swiftCode = map(row);
                if (swiftCode.isHeadquarter()) {
                    headquartersMap.put(swiftCode.getSwiftCode().substring(0, 8), swiftCode);
                }

                swiftCodes.add(swiftCode);
            }
        } catch (IOException e) {
            log.error("Error occurred while parsing excel file: " + EXCEL_FILE_PATH);
            throw new RuntimeException(e);
        }
        swiftCodeRepository.saveAll(swiftCodes);

        for (SwiftCode swiftCode : swiftCodes) {
            if (!swiftCode.isHeadquarter()) {
                String headquarterKey = swiftCode.getSwiftCode().substring(0, 8);
                SwiftCode headquarter = headquartersMap.get(headquarterKey);
                if (headquarter != null) {
                    swiftCode.setHeadquarterId(headquarter);
                    headquarter.addBranch(swiftCode);
                }
            }
        }
        swiftCodeRepository.saveAll(swiftCodes);
    }
}
