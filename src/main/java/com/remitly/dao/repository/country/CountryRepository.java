package com.remitly.dao.repository.country;

import com.remitly.dao.model.country.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findById(Long id);
    Optional<Country> findByCountryISO2code(String countryISO2code);

}
