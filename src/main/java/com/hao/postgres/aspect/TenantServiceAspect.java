package com.hao.postgres.aspect;

import com.hao.postgres.jpa.entity.TenantAware;
import com.hao.postgres.util.HibernateFilterHelper;
import com.hao.postgres.util.MyJpaRepositoryImpl;
import com.hao.postgres.util.SecurityContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.hibernate.Session;
import org.springframework.aop.framework.Advised;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TenantServiceAspect {


    @Pointcut("execution(public * org.springframework.data.repository.Repository+.*(..))")
    void isRepository() {
        /* aspect */
    }

    @Pointcut(value = "isRepository()")
    void enableMultiTenancy() {
        /* aspect */
    }

    @Before("within(org.springframework.data.repository.Repository+) && target(repo)")
    public void beforeCall(JoinPoint jp, Advised repo) {
        log.info("Before calling repository: {}", repo);
    }

    @After("within(org.springframework.data.repository.Repository+) && target(repo)")
    public void afterCall(JoinPoint jp, Advised repo) {
        log.info("After calling repository: {}", repo);
    }



    // @Around("execution(public * *(..)) && enableMultiTenancy() && target(repo)")
    @Around("within(org.springframework.data.repository.Repository+) && target(repo)")
    public Object aroundCall(final ProceedingJoinPoint pjp, Advised repo) throws Throwable {
        log.info("(Around)Before calling repo: {}", repo);
        HibernateFilterHelper.clear();
        Object target = repo.getTargetSource().getTarget();
        if (target instanceof SimpleJpaRepository) {
            MyJpaRepositoryImpl<?, ?> repoImpl = (MyJpaRepositoryImpl<?, ?>) target;
            Session session = repoImpl.getEntityManager().unwrap(Session.class);
            System.out.println("session: " + session);
            boolean enableTenantFilter = false;

            if (TenantAware.class.isAssignableFrom(repoImpl.getEntityInformation().getJavaType())) {
                enableTenantFilter = true;
                System.out.println("yes");

            }
            if (enableTenantFilter) {
                if (!repoImpl.getEntityManager().isJoinedToTransaction()) {
                    // enable filter when transaction is open
                    HibernateFilterHelper.setEnableTenantFilter(true);
                    System.out.println("Not joined to TX yet");
                    HibernateFilterHelper.enableTenantFilter(session);
                } else {
                    HibernateFilterHelper.enableTenantFilter(session);
                    log.info("enabled tenant filter for tenantId {}: {}",
                            SecurityContextUtils.getTenantId(), pjp);
                    System.out.println("Already joined");
                }
            } else {
                HibernateFilterHelper.disableTenantFilter(session);
                log.info("disabled tenant filter for {}", pjp);
            }

        }

//        final Filter filter =
//                this.entityManager
//                        .unwrap(Session.class) // requires transaction
//                        .enableFilter(TenantAwareEntity.TENANT_FILTER_NAME)
//                        .setParameter(
//                                TenantAwareEntity.TENANT_FILTER_ARGUMENT_NAME, resolveCurrentTenantIdentifier());
//        filter.validate();
        try {
            return pjp.proceed();
        } finally {
            log.info("(Around)After calling repo: {}", repo);
            HibernateFilterHelper.clear();
        }

    }


}
