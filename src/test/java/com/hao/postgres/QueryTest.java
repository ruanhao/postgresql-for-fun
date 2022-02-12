package com.hao.postgres;

import com.hao.postgres.jpa.repo.CityRepository;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Slf4j
@Sql("/tbl_cities.sql")
public class QueryTest {

    @Autowired
    CityRepository cityRepository;

    @Test
    public void find() {
        var allCities = cityRepository.findAll();
        Assertions.assertEquals(50, allCities.size());

        var page = cityRepository.findAll(PageRequest.of(0, 10));
        Assertions.assertEquals(50, page.getTotalElements());
        Assertions.assertEquals(5, page.getTotalPages());
        var pageOfCities = page.stream().collect(Collectors.toList());
        Assertions.assertEquals(1L, pageOfCities.get(0).getId());
        Assertions.assertEquals(10L, pageOfCities.get(pageOfCities.size() - 1).getId());
    }


}
