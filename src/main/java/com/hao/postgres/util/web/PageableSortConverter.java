package com.hao.postgres.util.web;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.hao.postgres.annotation.ClassMapping;
import com.hao.postgres.annotation.SortField;
import com.hao.postgres.annotation.SortMapping;
import com.hao.postgres.annotation.SortMappings;
import com.hao.postgres.jpa.entity.MyEntity;
import com.hao.postgres.util.JsonUtils;
import com.hao.postgres.config.MapperCache;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

@Slf4j
public class PageableSortConverter {
    private static final ConcurrentMap<Class<?>, List<BeanPropertyDefinition>> beanPropertiesMap = new ConcurrentHashMap<>();
    private PageableSortConverter() {
    }

    public static Pageable convert(Pageable pageable, ClassMapping classMapping) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        Pageable convertedPageable;
        if (classMapping == null) {
            convertedPageable = PageRequest.of(page, size, kebab2Camel(pageable.getSort()));
        } else {
            convertedPageable = PageRequest.of(page, size, convertSortByDto(pageable.getSort(), classMapping));
        }
        log.info("pageable conversion from {} to {}", pageable, convertedPageable);
        return convertedPageable;
    }

    private static Sort kebab2Camel(final Sort sort) {
        List<Order> orders = new LinkedList<>();
        sort.forEach(order -> {
            String property = order.getProperty();
            if (property == null || !property.contains("-")) {
                orders.add(order);
            } else {
                String[] fields = property.split("\\.");
                String camelCaseProperty = Stream.of(fields)
                    .map(x -> x.contains("-") ? CaseUtils.toCamelCase(x, false, '-') : x).collect(Collectors.joining("."));
                orders.add(new Order(order.getDirection(), camelCaseProperty));
            }
        });

        return Sort.by(orders);
    }

    private static Sort convertSortByDto(final Sort sort, final ClassMapping classMapping) {
        final Class<?> dtoClass = classMapping.dtoClass();
        final Class<? extends MyEntity> entityClass = classMapping.entityClass();
        List<Order> orders = new LinkedList<>();
        sort.forEach(originalOrder -> {
            String originalProperty = originalOrder.getProperty();
            if (originalProperty == null) {
                orders.add(originalOrder);
            } else {
                // first priority convert to @SortMapping on DTO class
                Optional<Order> convertedPropertyOpt = convertBySortMapping(dtoClass, originalProperty, originalOrder.getDirection());

                if (!convertedPropertyOpt.isPresent()) {
                    // second priority convert to @SortField or field name (or entity field name from DTO field name by mapper configuration)
                    convertedPropertyOpt = convertByField(dtoClass, entityClass, originalProperty, originalOrder.getDirection());
                }

                if (convertedPropertyOpt.isPresent()) {
                    orders.add(convertedPropertyOpt.get());
                } else {
                    // fallback to original one
                    orders.add(originalOrder);
                }
            }
        });

        return Sort.by(orders);
    }

    @SuppressWarnings("squid:S3776")
    private static Optional<Order> convertByField(final Class<?> dtoClass, final Class<? extends MyEntity> entityClass,
        final String originalSortProperty, final Direction originalDirection) {
        String[] fields = originalSortProperty.split("\\.");
        List<String> convertedFields = new LinkedList<>();
        boolean reverseDirection = false;

        List<BeanPropertyDefinition> beanProperties = getBeanProperties(dtoClass);
        // for a recursive property such as "node.system.hostname", check field one by one
        boolean convertedBySortField = false;
        int index = 0;
        for (String field : fields) {
            Optional<BeanPropertyDefinition> bpdOpt = JsonUtils.findBeanProperty(field, beanProperties);
            if (bpdOpt.isPresent()) {
                BeanPropertyDefinition bpd = bpdOpt.get();
                convertedFields.add(bpd.getField().getName());

                if (++index < fields.length) {
                    // get bean properties for class of this field, then look into next field
                    beanProperties = getBeanProperties(bpd.getField().getType());
                } else {
                    // leaf field, check if there is @SortField, if present, it will take precedence
                    SortField sortField = bpd.getField().getAnnotation(SortField.class);
                    if (sortField != null) {
                        convertedFields.clear(); // if annotated, use the value from annotation
                        convertedFields.add(sortField.value());
                        reverseDirection = sortField.reverseDirection();
                        convertedBySortField = true;
                    }
                }
            } else {
                return Optional.empty();
            }
        }

        if (convertedBySortField || ClassMapping.VoidEntity.class.equals(entityClass)) {
            return Optional.of(new Order(reverseDirection ? reverse(originalDirection): originalDirection, String.join(".", convertedFields)));
        } else {
            // find field in entity class by mapper configuration
            String dtoField = String.join(".", convertedFields);
            String entityField = MapperCache.getFieldMapping(dtoClass, entityClass).get(dtoField);
            entityField = entityField == null ? dtoField : entityField;
            return Optional.of(new Order(originalDirection, entityField));
        }
    }

    private static Optional<Order> convertBySortMapping(Class<?> dtoClass, String originalSortProperty, Direction originalDirection) {
        SortMapping sortMapping = dtoClass.getAnnotation(SortMapping.class);
        if (sortMapping != null && Objects.equals(originalSortProperty, sortMapping.from())) {
            boolean reverseDirection = sortMapping.reverseDirection();
            return Optional.of(new Order(reverseDirection ? reverse(originalDirection): originalDirection, sortMapping.to()));
        }

        SortMappings sortMappings = dtoClass.getAnnotation(SortMappings.class);
        if (sortMappings != null) {
            for (SortMapping sm : sortMappings.value()) {
                if (sm != null && Objects.equals(originalSortProperty, sm.from())) {
                    boolean reverseDirection = sm.reverseDirection();
                    return Optional.of(new Order(reverseDirection ? reverse(originalDirection): originalDirection, sm.to()));
                }
            }
        }

        return Optional.empty();
    }

    private static List<BeanPropertyDefinition> getBeanProperties(final JavaType javaType) {
        return getBeanProperties(javaType.getRawClass());
    }

    private static List<BeanPropertyDefinition> getBeanProperties(final Class<?> clazz) {
        return beanPropertiesMap.computeIfAbsent(clazz, k -> {
            log.info("get bean properties for type {}", k);
            return JsonUtils.getBeanProperties(clazz);
        });
    }

    private static Direction reverse(Direction d) {
        switch (d) {
        case ASC:
            return Direction.DESC;
        case DESC:
            return Direction.ASC;
        default:
            break;
        }
        return d;
    }

}
