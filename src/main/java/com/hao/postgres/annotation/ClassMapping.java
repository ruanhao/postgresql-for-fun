package com.hao.postgres.annotation;

import com.hao.postgres.jpa.entity.MyEntity;
import java.lang.annotation.*;

/**
 * Annotation on a method of a controller class to set mapping between DTO and Entity classes.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ClassMapping {

    Class<?> dtoClass();

    Class<? extends MyEntity> entityClass() default VoidEntity.class;

    class VoidEntity extends MyEntity {
    }
}
