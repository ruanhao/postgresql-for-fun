package com.hao.postgres.config;

import com.hao.postgres.dto.PersonDto;
import com.hao.postgres.jpa.entity.Person;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyMapperConfigurer implements MapperConfigurer {

    @Override
    public MapperFactory configure(MapperFactory factory) {
        log.info("Configuring mapper ...");
        factory.classMap(PersonDto.class, Person.class)
                .field("nickName", "name")
                .field("info.gender", "gender")
                .byDefault()
                .register();

        return factory;
    }
}

interface MapperConfigurer {
    MapperFactory configure(MapperFactory factory);
}
