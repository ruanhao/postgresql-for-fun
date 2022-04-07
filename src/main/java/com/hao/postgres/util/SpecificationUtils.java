package com.hao.postgres.util;

import com.hao.postgres.annotation.Searchable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;


public class SpecificationUtils {

    public static <T> Specification<T> search(final String search, final Class<T> cls) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(search)) {
            throw new RuntimeException("no search input");
        }
        return search(Set.of(org.apache.commons.lang3.StringUtils.split(search, ",")), cls);
    }

    public static <T> Specification<T> search(final Set<String> searchStrings, final Class<T> cls) {
        if (CollectionUtils.isEmpty(searchStrings)) {
            throw new RuntimeException("no search strings input");
        }
        Set<String> notBlankSearchStrings = new HashSet<>();
        searchStrings.stream().filter(StringUtils::isNotBlank).map(String::trim).forEach(notBlankSearchStrings::add);
        if (CollectionUtils.isEmpty(notBlankSearchStrings)) {
            throw new RuntimeException("no search strings nput");
        }
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(cls, Searchable.class);
        if (CollectionUtils.isEmpty(fields)) {
            throw new RuntimeException("no @Searchable fields in class " + cls.getName());
        }
        List<Specification<T>> specs = new ArrayList<>();
        for (Field f : fields) {
            for (String search : notBlankSearchStrings) {
                specs.add((root, query, builder) -> builder.like(builder.lower(root.get(f.getName())), "%" + search.toLowerCase() + "%"));
            }
        }
        return or(specs);
    }

    public static <T> Specification<T> or(final List<Specification<T>> specs) {
        if (CollectionUtils.isEmpty(specs)) {
            throw new RuntimeException("no specs input");
        }
        Specification<T> spec = specs.get(0);
        for (int index = 1; index < specs.size(); ++index) {
            spec = spec.or(specs.get(index));
        }
        return spec;
    }
}
