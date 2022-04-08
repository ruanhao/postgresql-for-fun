package com.hao.postgres.annotation;

import java.lang.annotation.*;

/**
 * Wrapper annotation to allow declaring multiple {@link SortMapping} annotations on a DTO class.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SortMappings {
    /**
     * The individual {@link SortMapping} declarations for sort mappings.
     */
    SortMapping[] value();
}