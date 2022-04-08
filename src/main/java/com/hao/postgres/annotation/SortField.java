package com.hao.postgres.annotation;

import java.lang.annotation.*;

/**
 * Annotation on a field of a DTO class to set sort field when injecting a
 * {@link org.springframework.data.domain.Pageable} into a controller method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SortField {
    /**
     * Sort field for this attribute in DTO class.
     */
    String value();

    /**
     * Whether the original sort direction should be reversed.
     */
    boolean reverseDirection() default false;
}
