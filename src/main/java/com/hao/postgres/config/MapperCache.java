package com.hao.postgres.config;


import com.hao.postgres.jpa.entity.MyEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.TypeFactory;
import org.springframework.util.CollectionUtils;

@Slf4j
public class MapperCache {
    private static MapperFactory mapperFactory;
    private static final ConcurrentMap<MapperKey, Map<String, String>> fieldMappings = new ConcurrentHashMap<>();
    private MapperCache() {
    }

    static synchronized void initialize(MapperFactory factory) {
        mapperFactory = factory;
    }

    public static Map<String, String> getFieldMapping(final Class<?> dtoClass, final Class<? extends MyEntity> entityClass) {
        MapperKey key = new MapperKey(TypeFactory.valueOf(dtoClass), TypeFactory.valueOf(entityClass));
        if (fieldMappings.containsKey(key)) {
            return fieldMappings.get(key);
        }
        var classMap = mapperFactory.getClassMap(key);
        if (classMap == null) {
            return Map.of();
        }
        Set<FieldMap> fieldMaps = classMap.getFieldsMapping();
        if (CollectionUtils.isEmpty(fieldMaps)) {
            return Map.of();
        }
        Map<String, String> mappings = new HashMap<>();
        boolean isDtoAType = dtoClass.equals(classMap.getAType().getRawType());
        for (var fieldMap : fieldMaps) {
            if (isDtoAType) {
                mappings.put(fieldMap.getSourceExpression(), fieldMap.getDestinationExpression());
            } else {
                mappings.put(fieldMap.getDestinationExpression(), fieldMap.getSourceExpression());
            }
        }
        log.info("saving field mappings {} -> {}", key, mappings);
        fieldMappings.put(key, mappings);
        return mappings;
    }
}
