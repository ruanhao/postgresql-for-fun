package com.hao.postgres.controller;

import com.hao.postgres.annotation.ClassMapping;
import com.hao.postgres.dto.PersonDto;
import com.hao.postgres.jpa.entity.Person;
import com.hao.postgres.jpa.repo.PersonRepository;
import com.hao.postgres.util.DbUtils;
import com.hao.postgres.util.SecurityContextUtils;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/person"})
@Slf4j
public class PeopleController {

    @Autowired
    MapperFacade mapper;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    DataSource ds;

    @Value("classpath:sql/person.sql")
    Resource scriptResource;

    @GetMapping("/restore")
    @SneakyThrows
    public void restoreTable() {
        DbUtils.executeSqlScript(ds.getConnection(), scriptResource);
    }

    @GetMapping

    // curl -H 'x-tenant-id: cisco' 'localhost:8080/person?sort=first-name' | jq
    public List<PersonDto> query(Pageable pageable) {
        log.info("Qeuring person [{}]", SecurityContextUtils.getRequestContext());
        return personRepository.findAll(pageable).stream()
                .map(p -> mapper.map(p, PersonDto.class)).collect(Collectors.toList());
    }

    @GetMapping("/class-mapping")
    @ClassMapping(dtoClass = PersonDto.class, entityClass = Person.class)
    // curl -H 'x-tenant-id: cisco' 'localhost:8080/person/class-mapping?sort=nick-name' | jq
    public List<PersonDto> queryWithClassMapping(Pageable pageable) {
        return personRepository.findAll(pageable).stream()
                .map(p -> mapper.map(p, PersonDto.class)).collect(Collectors.toList());
    }

}
