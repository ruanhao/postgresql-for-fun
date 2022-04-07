package com.hao.postgres.jpa.repo;

import javax.persistence.EntityManager;
import lombok.Getter;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class EntityInfoAwareJpaRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> {

    @Getter
    private final EntityManager entityManager;

    @Getter
    private final JpaEntityInformation<T, ID> entityInformation;

    public EntityInfoAwareJpaRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
    }
}
