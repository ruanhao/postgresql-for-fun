package com.hao.postgres.util;

import com.hao.postgres.jpa.entity.TenantAwareEntity;
import java.util.Optional;
import javax.persistence.EntityManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

@Slf4j
public class MyJpaRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> {

    @Getter
    private final EntityManager entityManager;

    @Getter
    private final JpaEntityInformation<T, ID> entityInformation;

    public MyJpaRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
    }

    @Override
    public Optional<T> findById(ID id) {
        Optional<T> resultOpt = super.findById(id);
        Session session = entityManager.unwrap(Session.class);
        if (resultOpt.isPresent() && HibernateFilterHelper.isTenantFilterEnabled(session)) {
            T result = resultOpt.get();
            if (result instanceof TenantAwareEntity &&
                !((TenantAwareEntity) result).getTenantId().equals(HibernateFilterHelper.getTenantFilterValue(session))) {
                log.warn("access {}/{} is denied as tenant id not match", result.getClass().getSimpleName(), id);
                return Optional.empty();
            }
        }
        return resultOpt;
    }
}
