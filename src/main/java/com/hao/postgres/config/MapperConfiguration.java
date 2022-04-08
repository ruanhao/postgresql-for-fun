package com.hao.postgres.config;

import java.util.List;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

@Configuration
public class MapperConfiguration {

    @Bean
    public MapperFacade beanMapper(List<MapperConfigurer> configurers) {
        return new ConfigurableMapper() {
            @Override
            protected void configureFactoryBuilder(DefaultMapperFactory.Builder factoryBuilder) {
                factoryBuilder.mapNulls(false);
            }

            @Override
            protected void configure(MapperFactory factory) {
                if (!CollectionUtils.isEmpty(configurers)) {
                    configurers.forEach(x -> x.configure(factory));
                }
                MapperCache.initialize(factory);
            }
        };
    }
}