package com.hao.postgres.annotation;

import java.lang.annotation.*;

/**
 * Annotation on a DTO class to set sort mapping when injecting a
 * {@link org.springframework.data.domain.Pageable} into a controller method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(SortMappings.class)
public @interface SortMapping {
    /**
     * Unused.
     */
    String value() default "";

    /**
     * The sort field in uri parameter {@code sort}.
     */
    String from();

    /**
     * The sort field in database query.
     */
    String to();

    /**
     * Whether the original sort direction should be reversed.
     */
    boolean reverseDirection() default false;
}
