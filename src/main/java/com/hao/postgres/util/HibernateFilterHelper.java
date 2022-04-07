package com.hao.postgres.util;


import com.hao.postgres.jpa.entity.TenantAwareEntity;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Filter;
import org.hibernate.Session;

@Slf4j
public class HibernateFilterHelper {
    private static final String TENANT_FILTER_NAME = TenantAwareEntity.TENANT_FILTER_NAME;
    private static final ThreadLocal<Boolean> shouldEnableTenantFilterForCurrentThread = new ThreadLocal<>();

    private HibernateFilterHelper() {
    }

    public static Filter enableTenantFilter(Session session) {
        log.info("Enabling tenant filter for session: {}", session);
        Filter filter = session.enableFilter(TENANT_FILTER_NAME);
        filter.setParameter("tenantId", SecurityContextUtils.getTenantId());
        filter.validate();
        return filter;
    }

    public static void disableTenantFilter(Session session) {
        session.disableFilter(TENANT_FILTER_NAME);
    }

    static boolean isTenantFilterEnabled(Session session) {
        return session.getEnabledFilter(TENANT_FILTER_NAME) != null;
    }

    static String getTenantFilterValue(Session session) {
        if (isTenantFilterEnabled(session)) {
            return SecurityContextUtils.getTenantId();
        }
        return null;
    }

    public static void setEnableTenantFilter(boolean enable) {
        shouldEnableTenantFilterForCurrentThread.set(Boolean.valueOf(enable));
    }

    public static boolean shouldEnableTenantFilter() {

        boolean result = Boolean.TRUE.equals(shouldEnableTenantFilterForCurrentThread.get());
        log.info("Check if need enabling tenant filter [{}]", result);
        return result;
    }

    public static void clear() {
        shouldEnableTenantFilterForCurrentThread.remove();
    }
}
