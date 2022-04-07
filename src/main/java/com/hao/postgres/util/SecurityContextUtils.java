package com.hao.postgres.util;

import com.hao.postgres.dto.RequestContext;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecurityContextUtils {

    private static final ThreadLocal<RequestContext> contextOfCurrentThread = new ThreadLocal<>();

    private SecurityContextUtils() {
    }

    public static void setRequestContext(final String tenantId) {
        log.info("Setting request context, tenantId: {}", tenantId);
        contextOfCurrentThread.set(RequestContext.of(tenantId));
    }

    public static RequestContext getRequestContext() {
        return Objects.requireNonNull(contextOfCurrentThread.get(), "Request context is not available");
    }

    public static void clear() {
        contextOfCurrentThread.remove();
    }

    public static String getTenantId() {
        return getRequestContext().getTenantId();
    }


}
