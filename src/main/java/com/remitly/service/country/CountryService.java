package com.remitly.service.country;

import com.remitly.controller.exception.country.CountryNotFoundException;
import com.remitly.dao.model.country.Country;
import com.remitly.dao.repository.country.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    public Country getCountryById(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("There is no such country with id: {}", id);
                    return new CountryNotFoundException("There is no country with provided id");
                });
    }

    public Country getCountryByCountryISO2Code(String countryISO2code) {
        return countryRepository.findByCountryISO2code(countryISO2code)
                .orElseThrow(() -> {
                    log.error("There is no such country with countryISO2code: {}", countryISO2code);
                    return new CountryNotFoundException("There is no country with provided countryISO2code");
                });
    }
}
