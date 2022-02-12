package com.hao.postgres.aspect;

import com.hao.postgres.jpa.entity.TenantAwareEntity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TenantServiceAspect {


    @PersistenceContext
    public EntityManager entityManager;

    @Pointcut("execution(public * org.springframework.data.repository.Repository+.*(..))")
    void isRepository() {
        /* aspect */
    }

    @Pointcut(value = "isRepository()")
    void enableMultiTenancy() {
        /* aspect */
    }

    String resolveCurrentTenantIdentifier() {
        return "tenant-id";
    }

    @Around("execution(public * *(..)) && enableMultiTenancy()")
    public Object aroundExecution(final ProceedingJoinPoint pjp) throws Throwable {
        log.info("Proceeding joint point: {}", pjp);
        final Filter filter =
                this.entityManager
                        .unwrap(Session.class) // requires transaction
                        .enableFilter(TenantAwareEntity.TENANT_FILTER_NAME)
                        .setParameter(
                                TenantAwareEntity.TENANT_FILTER_ARGUMENT_NAME, resolveCurrentTenantIdentifier());
        filter.validate();
        return pjp.proceed();
    }


}
